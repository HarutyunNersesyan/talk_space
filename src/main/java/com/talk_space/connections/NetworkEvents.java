package com.talk_space.connections;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import java.util.Map;

@Component @RequiredArgsConstructor
public class NetworkEvents {
    private final ApplicationEventPublisher events;
    private final SimpMessagingTemplate websocket;
    public record Changed(String recipient) {}
    public void changed(String a, String b) { events.publishEvent(new Changed(a)); events.publishEvent(new Changed(b)); }
    @TransactionalEventListener
    public void deliver(Changed event) {
        try { websocket.convertAndSendToUser(event.recipient(), "/queue/connection-notifications", Map.of("type","NETWORK_CHANGED")); }
        catch (RuntimeException ignored) { /* REST synchronisation remains available. */ }
    }
}
