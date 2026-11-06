package com.talk_space.modules.discovery.internal.cleanup;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import com.talk_space.modules.users.api.UserDeleting;
import com.talk_space.modules.network.api.PairUnblocked;
import com.talk_space.shared.infrastructure.RedisSupport;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.context.event.EventListener;
import lombok.RequiredArgsConstructor;
import java.util.Map;
@Component @RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class DiscoveryAccountCleanup {
    private final NamedParameterJdbcTemplate jdbc;
    private final RedisSupport redis;
    @org.springframework.core.annotation.Order(0)
    @EventListener public void remove(UserDeleting event) {
        jdbc.update("delete from match_impressions where viewer=:name or candidate=:name",Map.of("name",event.userName()));
        redis.evict("matching:"+event.userName());
    }
    @org.springframework.core.annotation.Order(0)
    @EventListener public void unblocked(PairUnblocked event) {
        jdbc.update("delete from match_impressions where (viewer=:a and candidate=:b) or (viewer=:b and candidate=:a)",Map.of("a",event.actor(),"b",event.target()));
    }
}
