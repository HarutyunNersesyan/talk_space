package com.talk_space.notifications;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
@Component @RequiredArgsConstructor
public class NotificationDelivery {
    private static final Logger log = LoggerFactory.getLogger(NotificationDelivery.class);
    private final SimpMessagingTemplate websocket;
    @TransactionalEventListener
    public void afterCommit(NotificationService.Created event) {
        try {
            websocket.convertAndSendToUser(event.notification().getRecipient(),
                "/queue/connection-notifications", event.notification());
        } catch (RuntimeException ex) {
            // The durable row remains available through the REST inbox after reconnect.
            log.warn("WebSocket delivery failed for notification {}", event.notification().getId());
        }
    }
}
