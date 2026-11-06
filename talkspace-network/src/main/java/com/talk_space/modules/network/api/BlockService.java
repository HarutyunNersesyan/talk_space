package com.talk_space.modules.network.api;
import com.talk_space.modules.network.internal.persistence.ConnectionRepository;
import com.talk_space.modules.network.internal.persistence.UserBlockRepository;

import com.talk_space.modules.notifications.api.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service @RequiredArgsConstructor
public class BlockService {
    private final UserBlockRepository blocks;
    private final ConnectionRepository connections;
    private final NotificationService notifications;
    private final NetworkAccess access;
    private final NetworkEvents events;
    private final org.springframework.context.ApplicationEventPublisher publisher;

    @Transactional
    public UserBlock block(String actor, String target) {
        if (actor.equals(target)) throw new ResponseStatusException(BAD_REQUEST,"Cannot block yourself");
        access.lockPair(actor,target); access.member(actor); access.member(target);
        UserBlock block=blocks.findByBlockerAndBlocked(actor,target).orElseGet(()->{
            UserBlock b=new UserBlock(); b.setBlocker(actor); b.setBlocked(target); b.setCreatedAt(Instant.now()); return blocks.saveAndFlush(b);
        });
        var pair=access.pair(actor,target);
        connections.findByPairLowAndPairHigh(pair.get(0),pair.get(1)).ifPresent(c->{
            c.setStatus(Connection.State.REMOVED); c.setUpdatedAt(Instant.now()); connections.save(c);
        });
        notifications.deleteBetween(actor,target);
        events.changed(actor,target);
        return block;
    }
    @Transactional
    public void unblock(String actor, String target) {
        if (actor.equals(target)) throw new ResponseStatusException(BAD_REQUEST);
        access.lockPair(actor,target);
        blocks.findByBlockerAndBlocked(actor,target).ifPresent(blocks::delete);
        blocks.flush();
        publisher.publishEvent(new com.talk_space.modules.network.api.PairUnblocked(actor,target));
        events.changed(actor,target);
    }
    @Transactional(readOnly=true)
    public Page<UserBlock> list(String actor,int page,int size) {
        return blocks.findByBlockerOrderByCreatedAtDescIdDesc(actor,PageRequest.of(page,size));
    }
}
