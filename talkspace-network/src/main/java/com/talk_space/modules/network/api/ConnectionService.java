package com.talk_space.modules.network.api;
import com.talk_space.modules.users.api.model.User;
import com.talk_space.modules.network.internal.persistence.ConnectionRepository;
import com.talk_space.modules.users.api.model.Status;
import com.talk_space.modules.users.api.model.Role;
import com.talk_space.modules.users.api.UserDirectory;
import com.talk_space.modules.notifications.api.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.*;
import java.time.Instant;
import java.util.*;
import static org.springframework.http.HttpStatus.*;
@Service @RequiredArgsConstructor
public class ConnectionService {
    private final ConnectionRepository connections;
    private final UserDirectory users;
    private final NotificationService notifications;
    private final NetworkAccess access;
    @Transactional
    public Connection request(String sender, String receiver) {
        if (sender.equals(receiver)) throw new ResponseStatusException(BAD_REQUEST, "Cannot connect to yourself");
        lockPair(sender, receiver);
        access.requireVisible(sender,receiver);
        var target = users.findUserByUserName(receiver).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        if (target.getStatus() != Status.ACTIVE || target.getRole() != Role.USER || !Boolean.TRUE.equals(target.getVerifyMail()))
            throw new ResponseStatusException(BAD_REQUEST, "User is unavailable");
        var pair = pair(sender, receiver);
        Connection c = connections.findByPairLowAndPairHigh(pair.get(0), pair.get(1)).orElseGet(Connection::new);
        if (c.getStatus() == Connection.State.ACCEPTED) throw new ResponseStatusException(CONFLICT, "Already connected");
        if (c.getStatus() == Connection.State.PENDING) {
            if (sender.equals(c.getSender())) return c;
            throw new ResponseStatusException(CONFLICT, "Accept the incoming request instead");
        }
        c.setPairLow(pair.get(0)); c.setPairHigh(pair.get(1)); c.setSender(sender); c.setReceiver(receiver);
        c.setStatus(Connection.State.PENDING); c.setCreatedAt(Instant.now()); c.setUpdatedAt(c.getCreatedAt());
        c = connections.saveAndFlush(c);
        notifications.create(receiver, sender, "CONNECTION_REQUEST", c.getId());
        return c;
    }
    @Transactional
    public Connection transition(String actor, long id, String action) {
        Connection existing = connections.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        if (!actor.equals(existing.getSender()) && !actor.equals(existing.getReceiver())) throw new ResponseStatusException(NOT_FOUND);
        lockPair(existing.getSender(), existing.getReceiver());
        // Refresh after acquiring the pair lock: concurrent transitions may have committed while we waited.
        entityManager.refresh(existing);
        access.requireVisible(existing.getSender(),existing.getReceiver());
        Connection.State next;
        switch (action) {
            case "accept", "reject" -> {
                if (!actor.equals(existing.getReceiver())) throw new ResponseStatusException(FORBIDDEN);
                next = action.equals("accept") ? Connection.State.ACCEPTED : Connection.State.REJECTED;
            }
            case "cancel" -> {
                if (!actor.equals(existing.getSender())) throw new ResponseStatusException(FORBIDDEN);
                next = Connection.State.CANCELLED;
            }
            case "remove" -> next = Connection.State.REMOVED;
            default -> throw new ResponseStatusException(BAD_REQUEST, "Unknown action");
        }
        if (existing.getStatus() == next) return existing;
        var expected = action.equals("remove") ? Connection.State.ACCEPTED : Connection.State.PENDING;
        if (existing.getStatus() != expected) throw new ResponseStatusException(CONFLICT, "Invalid connection transition");
        existing.setStatus(next); existing.setUpdatedAt(Instant.now());
        connections.save(existing);
        String recipient = actor.equals(existing.getSender()) ? existing.getReceiver() : existing.getSender();
        notifications.create(recipient, actor, "CONNECTION_" + next.name(), id);
        return existing;
    }
    private final jakarta.persistence.EntityManager entityManager;
    @Transactional(readOnly=true)
    public Page<Connection> list(String actor, int page, int size) {
        return connections.forActor(actor, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt", "id")));
    }
    public long pending(String actor) { return connections.countByReceiverAndStatus(actor, Connection.State.PENDING); }
    private List<String> pair(String a, String b) { var result = new ArrayList<>(List.of(a, b)); Collections.sort(result); return result; }
    private void lockPair(String a, String b) {
        for (String name : pair(a, b)) users.lockMatchingViewer(name).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    }
}
