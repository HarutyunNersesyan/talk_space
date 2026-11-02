package com.talk_space;

import com.talk_space.blocks.*;
import com.talk_space.connections.*;
import com.talk_space.matching.*;
import com.talk_space.model.domain.*;
import com.talk_space.model.dto.ChatMessageDto;
import com.talk_space.model.enums.*;
import com.talk_space.repository.*;
import com.talk_space.service.ChatService;
import com.talk_space.api.controller.publics.ChatController;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class ConnectOnlyHttpTest {
    @Autowired MockMvc http;
    @Autowired UserRepository users;
    @Autowired HobbyRepository hobbies;
    @Autowired SpecialityRepository specialities;
    @Autowired ConnectionService connections;
    @Autowired BlockService blocks;
    @Autowired UserBlockRepository blockRepository;
    @Autowired ChatService chat;
    @Autowired ChatController socket;
    @Autowired ChatRepository messages;
    @Autowired MatchingRepository matching;
    @Autowired ConnectionRepository connectionRecords;
    @BeforeEach void setup() {
        Hobby hobby=new Hobby("Music",null); hobbies.saveAndFlush(hobby);
        Speciality speciality=new Speciality("Java",null); specialities.saveAndFlush(speciality);
        for (String name:List.of("alice","bob","eve")) {
            User u=new User();u.setFirstName("Alice");u.setLastName("Smith");u.setUserName(name);
            u.setBirthDate(LocalDate.of(2000,1,1));u.setEmail(name+"@gmail.com");u.setGender(Gender.FEMALE);
            u.setAboutMe("Hello from "+name);u.setHobbies(new ArrayList<>(List.of(hobby)));u.setSpecialities(new ArrayList<>(List.of(speciality)));
            u.setPassword("hash");u.setRole(Role.USER);u.setStatus(Status.ACTIVE);u.setVerifyMail(true);users.save(u);
        }
        users.flush();
    }
    Connection accepted() { var c=connections.request("alice","bob"); return connections.transition("bob",c.getId(),"accept"); }
    @Test void requestContainsFullPublicProfileAndRecipientCanReadIt() throws Exception {
        connections.request("alice","bob");
        http.perform(get("/api/public/connections").with(user("bob@gmail.com")))
            .andExpect(status().isOk()).andExpect(jsonPath("content[0].profile.userName").value("alice"))
            .andExpect(jsonPath("content[0].profile.hobbies[0]").value("Music"))
            .andExpect(jsonPath("content[0].profile.specialities[0]").value("Java"))
            .andExpect(jsonPath("content[0].profile.about").value("Hello from alice"))
            .andExpect(jsonPath("content[0].profile.email").doesNotExist())
            .andExpect(jsonPath("content[0].profile.password").doesNotExist());
    }
    @Test void recentRequestsCoolDownAndBecomeDiscoverableAgain() {
        var c=connections.request("alice","bob");
        assertFalse(matching.candidates("alice",MatchMode.HOBBIES,Instant.now(),100).contains("bob"));
        assertFalse(matching.candidates("bob",MatchMode.HOBBIES,Instant.now(),100).contains("alice"));
        c.setUpdatedAt(Instant.now().minus(Duration.ofHours(25))); connectionRecords.saveAndFlush(c);
        assertTrue(matching.candidates("alice",MatchMode.HOBBIES,Instant.now(),100).contains("bob"));
        connections.transition("bob",c.getId(),"reject"); connectionRecords.flush();
        assertFalse(matching.candidates("alice",MatchMode.HOBBIES,Instant.now(),100).contains("bob"));
        c.setUpdatedAt(Instant.now().minus(Duration.ofDays(8))); connectionRecords.saveAndFlush(c);
        assertTrue(matching.candidates("alice",MatchMode.HOBBIES,Instant.now(),100).contains("bob"));
    }
    @Test void skipIsOwnedByActorSurvivesCycleResetAndExpires() throws Exception {
        matching.record("alice","bob",MatchMode.HOBBIES,Instant.now());
        http.perform(post("/api/public/matching/skip").with(user("eve@gmail.com"))
            .contentType("application/json").content("{\"userName\":\"bob\",\"mode\":\"HOBBIES\"}"))
            .andExpect(status().isNotFound());
        http.perform(post("/api/public/matching/skip").with(user("alice@gmail.com"))
            .contentType("application/json").content("{\"userName\":\"bob\",\"mode\":\"HOBBIES\"}"))
            .andExpect(status().isNoContent());
        matching.restartCycle("alice",MatchMode.HOBBIES);
        assertFalse(matching.candidates("alice",MatchMode.HOBBIES,Instant.now().plusSeconds(1),100).contains("bob"));
        assertTrue(matching.candidates("alice",MatchMode.SPECIALITIES,Instant.now().plusSeconds(1),100).contains("bob"));
    }
    @Test void agedSkipIsEligibleAndInvalidSkipPayloadIsRejected() throws Exception {
        matching.record("alice","bob",MatchMode.HOBBIES,Instant.now().minus(Duration.ofDays(2)));
        matching.skip("alice","bob",MatchMode.HOBBIES,Instant.now().minus(Duration.ofDays(2)));
        assertTrue(matching.candidates("alice",MatchMode.HOBBIES,Instant.now().plusSeconds(1),100).contains("bob"));
        http.perform(post("/api/public/matching/skip").with(user("alice@gmail.com"))
            .contentType("application/json").content("{\"userName\":\"bob\",\"mode\":\"INVALID\"}"))
            .andExpect(status().isBadRequest());
        http.perform(post("/api/public/matching/skip").with(user("alice@gmail.com"))
            .contentType("application/json").content("{\"userName\":\"\",\"mode\":\"HOBBIES\"}"))
            .andExpect(status().isBadRequest());
    }
    @Test void matchingEndpointReturnsServerCalculatedScoreAndExplanation() throws Exception {
        http.perform(get("/api/public/user/searchByHobbies/alice").with(user("alice@gmail.com")))
            .andExpect(status().isOk()).andExpect(jsonPath("matching.score").value(100.0))
            .andExpect(jsonPath("matching.commonHobbies[0]").value("Music"))
            .andExpect(jsonPath("matching.commonSpecialities[0]").value("Java"))
            .andExpect(jsonPath("matching.hobbyWeight").value(80.0))
            .andExpect(jsonPath("matching.algorithmVersion").value("2.0"));
    }
    @Test void httpSendConfirmsSaveAndDuplicateRetryDoesNotInsertTwice() throws Exception {
        accepted();
        String body="{\"receiver\":\"bob\",\"content\":\"hello\",\"clientMessageId\":\"550e8400-e29b-41d4-a716-446655440000\",\"sender\":\"eve\"}";
        for (int i=0; i<2; i++) http.perform(post("/api/public/chat/messages").with(user("alice@gmail.com"))
            .contentType("application/json").content(body)).andExpect(status().isOk())
            .andExpect(jsonPath("sender").value("alice")).andExpect(jsonPath("content").value("hello"));
        assertEquals(1,messages.count());
        String changed=body.replace("hello","different");
        http.perform(post("/api/public/chat/messages").with(user("alice@gmail.com"))
            .contentType("application/json").content(changed)).andExpect(status().isConflict());
        assertEquals(1,messages.count());
    }
    @Test void httpSendRejectsUnacceptedBlockedAndMalformedRequests() throws Exception {
        String body="{\"receiver\":\"bob\",\"content\":\"hello\",\"clientMessageId\":\"550e8400-e29b-41d4-a716-446655440000\"}";
        http.perform(post("/api/public/chat/messages").with(user("alice@gmail.com"))
            .contentType("application/json").content(body)).andExpect(status().isForbidden());
        http.perform(post("/api/public/chat/messages").with(user("alice@gmail.com"))
            .contentType("application/json").content(body.replace("550e8400-e29b-41d4-a716-446655440000","bad")))
            .andExpect(status().isBadRequest());
        accepted(); blocks.block("bob","alice");
        http.perform(post("/api/public/chat/messages").with(user("alice@gmail.com"))
            .contentType("application/json").content(body)).andExpect(status().isNotFound());
        assertEquals(0,messages.count());
    }
    @Test void pendingDoesNotGrantChatAccessAndCannotSpoofSender() throws Exception {
        connections.request("alice","bob");
        http.perform(get("/api/public/chat/history/alice/bob").with(user("alice@gmail.com"))).andExpect(status().isForbidden());
        ChatMessageDto dto=new ChatMessageDto();dto.setSender("eve");dto.setReceiver("bob");dto.setContent("hello");
        assertThrows(ResponseStatusException.class,()->socket.sendMessage(dto,()->"alice"));
        assertEquals(0,messages.count());
    }
    @Test void acceptedConnectionGrantsChatAndSocketSenderComesFromPrincipal() throws Exception {
        accepted();
        ChatMessageDto dto=new ChatMessageDto();dto.setSender("eve");dto.setReceiver("bob");dto.setContent("hello");
        socket.sendMessage(dto,()->"alice");
        assertEquals("alice",messages.findAll().get(0).getSender().getUserName());
        http.perform(get("/api/public/chat/history/alice/bob").with(user("alice@gmail.com")))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].content").value("hello"));
        http.perform(get("/api/public/chat/history/alice/bob").with(user("eve@gmail.com"))).andExpect(status().isForbidden());
        http.perform(post("/api/public/chat/read/alice/bob").with(user("eve@gmail.com"))).andExpect(status().isForbidden());
        http.perform(get("/api/public/chat/conversations/alice").with(user("eve@gmail.com"))).andExpect(status().isForbidden());
    }
    @Test void blockingHidesBothProfilesAndDiscoveryAndClosesChatImmediately() throws Exception {
        accepted();chat.send("alice","bob","before blocking");blocks.block("alice","bob");
        assertFalse(matching.candidates("alice",MatchMode.HOBBIES,Instant.now(),100).contains("bob"));
        assertFalse(matching.candidates("bob",MatchMode.HOBBIES,Instant.now(),100).contains("alice"));
        for (String[] pair:List.of(new String[]{"alice","bob"},new String[]{"bob","alice"})) {
            http.perform(get("/api/public/profiles/"+pair[1]).with(user(pair[0]+"@gmail.com"))).andExpect(status().isNotFound());
            http.perform(get("/api/public/chat/history/"+pair[0]+"/"+pair[1]).with(user(pair[0]+"@gmail.com"))).andExpect(status().isNotFound());
            http.perform(get("/api/public/user/image/"+pair[1]).with(user(pair[0]+"@gmail.com"))).andExpect(status().isNotFound());
            http.perform(get("/api/public/chat/conversations/"+pair[0]).with(user(pair[0]+"@gmail.com")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        }
        assertThrows(ResponseStatusException.class,()->chat.send("bob","alice","after blocking"));
        assertThrows(ResponseStatusException.class,()->connections.request("bob","alice"));
        assertEquals(1,messages.count());
    }
    @Test void unblockRestoresVisibilityButNeedsFreshConsentAndCannotUndoAnotherUsersBlock() throws Exception {
        accepted();blocks.block("alice","bob");blocks.block("bob","alice");blocks.unblock("alice","bob");
        http.perform(get("/api/public/profiles/bob").with(user("alice@gmail.com"))).andExpect(status().isNotFound());
        blocks.unblock("bob","alice");
        http.perform(get("/api/public/profiles/bob").with(user("alice@gmail.com"))).andExpect(status().isOk());
        assertThrows(ResponseStatusException.class,()->chat.send("alice","bob","still forbidden"));
        var fresh=connections.request("alice","bob");assertEquals(Connection.State.PENDING,fresh.getStatus());
        connections.transition("bob",fresh.getId(),"accept");
        assertNotNull(chat.send("alice","bob","new consent").getId());
    }
    @Test void blocksArePrivateIdempotentAndOwnedByAuthenticatedActor() throws Exception {
        http.perform(post("/api/public/blocks").with(user("alice@gmail.com")).contentType("application/json").content("{\"userName\":\"bob\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("blocker").value("alice"));
        blocks.block("alice","bob");assertEquals(1,blockRepository.count());
        http.perform(get("/api/public/blocks").with(user("bob@gmail.com"))).andExpect(jsonPath("content.length()").value(0));
        http.perform(delete("/api/public/blocks/alice").with(user("bob@gmail.com"))).andExpect(status().isOk());
        assertTrue(blockRepository.blockedEitherWay("alice","bob"));
        http.perform(delete("/api/public/blocks/bob").with(user("alice@gmail.com"))).andExpect(status().isOk());
        assertFalse(blockRepository.blockedEitherWay("alice","bob"));
    }
    @Test void removedConnectionsDoNotAllowOldHistoryOrSending() throws Exception {
        var c=accepted();chat.send("alice","bob","old message");connections.transition("alice",c.getId(),"remove");
        assertThrows(ResponseStatusException.class,()->chat.send("alice","bob","new message"));
        http.perform(get("/api/public/chat/history/alice/bob").with(user("alice@gmail.com"))).andExpect(status().isForbidden());
    }
    @Test void regularUsersCannotBypassProfileVisibilityViaAdminDirectory() throws Exception {
        http.perform(get("/api/private/admin/getAll").with(user("alice@gmail.com").authorities(()->"USER")))
            .andExpect(status().isForbidden());
    }
    @Test void likingEndpointsAreRemoved() throws Exception {
        http.perform(post("/api/public/user/like").with(user("alice@gmail.com"))).andExpect(status().is4xxClientError());
        http.perform(get("/api/public/user/like/get").with(user("alice@gmail.com"))).andExpect(status().isNotFound());
    }
}
