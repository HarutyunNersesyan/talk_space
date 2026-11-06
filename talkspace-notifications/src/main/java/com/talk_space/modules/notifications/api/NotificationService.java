package com.talk_space.modules.notifications.api;
import com.talk_space.modules.notifications.internal.persistence.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import static org.springframework.http.HttpStatus.NOT_FOUND;
@Service @RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notifications;
    private final ApplicationEventPublisher events;
    public record Created(Notification notification) {}
    @Transactional
    public void create(String recipient, String actor, String type, Long connectionId) {
        Notification n = new Notification(); n.setRecipient(recipient); n.setActor(actor);
        n.setType(type); n.setConnectionId(connectionId); n.setCreatedAt(Instant.now());
        notifications.save(n); events.publishEvent(new Created(n));
    }
    @Transactional(readOnly=true)
    public Page<Notification> list(String actor, int page, int size) {
        return notifications.findByRecipientOrderByCreatedAtDescIdDesc(actor, PageRequest.of(page, size));
    }
    public long unread(String actor) { return notifications.countByRecipientAndReadAtIsNull(actor); }
    @Transactional
    public void read(String actor, long id) {
        var n = notifications.findById(id).filter(x -> x.getRecipient().equals(actor))
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        if (n.getReadAt() == null) { n.setReadAt(Instant.now()); notifications.save(n); }
    }
    @Transactional
    public void deleteBetween(String a, String b) { notifications.deleteBetween(a,b); }
    @Transactional
    public void readAll(String actor) { notifications.readAll(actor, Instant.now()); }
}
