package com.talk_space.modules.presence.api;
import com.talk_space.shared.infrastructure.RedisSupport;
import com.github.benmanes.caffeine.cache.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
@Service @RequiredArgsConstructor
public class PresenceService {
    private final RedisSupport redis;
    private final Cache<String, Map<String, Long>> local = Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(3)).maximumSize(100000).build();
    private static final long LEASE_MS = 90000;
    private static final DefaultRedisScript<Long> TOUCH = new DefaultRedisScript<>("""
        redis.call('ZADD',KEYS[1],ARGV[1],ARGV[2]); redis.call('PEXPIRE',KEYS[1],ARGV[3]); return 1
        """, Long.class);
    private static final DefaultRedisScript<Long> ONLINE = new DefaultRedisScript<>("""
        redis.call('ZREMRANGEBYSCORE',KEYS[1],'-inf',ARGV[1]); return redis.call('ZCARD',KEYS[1])
        """, Long.class);
    public void heartbeat(String user, String session) {
        long expiry = System.currentTimeMillis() + LEASE_MS;
        if (redis.enabled()) try {
            redis.template().execute(TOUCH, List.of(key(user)), String.valueOf(expiry), session, String.valueOf(LEASE_MS + 10000)); return;
        } catch (Exception ex) { redis.failure("presence_write"); }
        local.get(user, k -> new ConcurrentHashMap<>()).put(session, expiry);
    }
    public void disconnect(String user, String session) {
        if (redis.enabled()) try { redis.template().opsForZSet().remove(key(user), session); }
        catch (Exception ex) { redis.failure("presence_disconnect"); }
        var sessions = local.getIfPresent(user); if (sessions != null) sessions.remove(session);
    }
    public boolean online(String user) {
        if (redis.enabled()) try {
            Long count = redis.template().execute(ONLINE, List.of(key(user)), String.valueOf(System.currentTimeMillis()));
            return count != null && count > 0;
        } catch (Exception ex) { redis.failure("presence_read"); }
        var sessions = local.getIfPresent(user); if (sessions == null) return false;
        long now = System.currentTimeMillis(); sessions.values().removeIf(expiry -> expiry <= now); return !sessions.isEmpty();
    }
    private String key(String user) { return "talkspace:presence:" + user; }
}
