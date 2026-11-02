package com.talk_space.infrastructure;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
@Component @RequiredArgsConstructor
public class PresenceSocketListener {
    private final PresenceService presence;
    @EventListener public void connected(SessionConnectedEvent event) {
        var a = StompHeaderAccessor.wrap(event.getMessage());
        if (event.getUser() != null) presence.heartbeat(event.getUser().getName(), a.getSessionId());
    }
    @EventListener public void disconnected(SessionDisconnectEvent event) {
        if (event.getUser() != null) presence.disconnect(event.getUser().getName(), event.getSessionId());
    }
}
