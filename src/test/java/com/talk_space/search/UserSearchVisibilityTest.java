package com.talk_space.search;

import com.talk_space.blocks.*;
import com.talk_space.model.domain.User;
import com.talk_space.model.enums.*;
import com.talk_space.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class UserSearchVisibilityTest {
    @Test void excludesBlocksInBothDirectionsAndRechecksDatabaseAfterIndexResponse() throws Exception {
        var gateway=mock(ElasticsearchGateway.class);var users=mock(UserRepository.class);var blocks=mock(UserBlockRepository.class);
        var service=new UserSearchService(gateway,users,new SimpleMeterRegistry(),blocks);
        when(gateway.enabled()).thenReturn(true);
        var outgoing=new UserBlock();outgoing.setBlocker("alice");outgoing.setBlocked("bob");
        var incoming=new UserBlock();incoming.setBlocker("carol");incoming.setBlocked("alice");
        when(blocks.involving("alice")).thenReturn(List.of(outgoing,incoming));
        ObjectMapper json=new ObjectMapper();
        when(gateway.encode(any())).thenAnswer(i->json.writeValueAsString(i.getArgument(0)));
        when(gateway.request(eq("POST"),eq("/talkspace-users/_search"),anyString(),eq("application/json"))).thenReturn(json.readTree("""
            {"hits":{"total":{"value":4},"hits":[{"_source":{"userName":"bob"}},{"_source":{"userName":"carol"}},{"_source":{"userName":"dave"}},{"_source":{"userName":"eve"}}]}}
            """));
        for (String name:List.of("bob","carol","dave","eve")) {
            var u=new User();u.setUserName(name);u.setRole(Role.USER);u.setStatus(Status.ACTIVE);u.setVerifyMail(true);
            when(users.findUserByUserName(name)).thenReturn(Optional.of(u));
        }
        // Dave was blocked after the request's exclusion list was constructed.
        when(blocks.blockedEitherWay("alice","dave")).thenReturn(true);
        var result=service.find("alice","Music",0,20);
        assertEquals(List.of("eve"),result.content().stream().map(p->p.getUserName()).toList());
        var encoded=org.mockito.ArgumentCaptor.forClass(Object.class);verify(gateway).encode(encoded.capture());
        var query=json.valueToTree(encoded.getValue()).path("query").path("bool").path("must_not").toString();
        assertTrue(query.contains("bob"));assertTrue(query.contains("carol"));assertTrue(query.contains("alice"));
    }
}
