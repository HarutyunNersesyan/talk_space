package com.talk_space.blocks;

import com.talk_space.connections.*;
import com.talk_space.notifications.NotificationRepository;
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
    private final NotificationRepository notifications;
    private final NetworkAccess access;
    private final NetworkEvents events;
    private final org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate jdbc;

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
        jdbc.update("delete from match_impressions where (viewer=:a and candidate=:b) or (viewer=:b and candidate=:a)",java.util.Map.of("a",actor,"b",target));
        events.changed(actor,target);
    }
    @Transactional(readOnly=true)
    public Page<UserBlock> list(String actor,int page,int size) {
        return blocks.findByBlockerOrderByCreatedAtDescIdDesc(actor,PageRequest.of(page,size));
    }
}
