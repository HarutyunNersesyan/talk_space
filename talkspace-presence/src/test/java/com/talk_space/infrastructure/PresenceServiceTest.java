package com.talk_space.infrastructure;
import com.talk_space.modules.presence.api.PresenceService;
import com.talk_space.shared.infrastructure.RedisSupport;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class PresenceServiceTest {
    @Test void oneClosingTabDoesNotMakeOtherTabOffline() {
        var service=new PresenceService(mock(RedisSupport.class));
        assertFalse(service.online("alice"));service.heartbeat("alice","tab1");service.heartbeat("alice","tab2");
        service.disconnect("alice","tab1");assertTrue(service.online("alice"));
        service.disconnect("alice","tab2");assertFalse(service.online("alice"));
    }
    @Test void disconnectIsIdempotentAndUsersAreIsolated() {
        var service=new PresenceService(mock(RedisSupport.class));
        service.heartbeat("alice","tab1");service.disconnect("bob","tab1");assertTrue(service.online("alice"));
        service.disconnect("alice","tab1");service.disconnect("alice","tab1");assertFalse(service.online("alice"));
    }
}
