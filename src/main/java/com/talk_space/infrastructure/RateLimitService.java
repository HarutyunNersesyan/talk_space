package com.talk_space.infrastructure;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
@Service @RequiredArgsConstructor
public class RateLimitService {
    private final RedisSupport redis;
    private static final DefaultRedisScript<Long> SCRIPT = new DefaultRedisScript<>("""
        local count = redis.call('INCR', KEYS[1])
        if count == 1 then redis.call('EXPIRE', KEYS[1], ARGV[2]) end
        if count > tonumber(ARGV[1]) then return math.max(1, redis.call('TTL', KEYS[1])) end
        return 0
        """, Long.class);
    public long retryAfter(String identity, String group, int limit, int seconds) {
        if (!redis.enabled()) return 0;
        try {
            String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(identity.getBytes(StandardCharsets.UTF_8)));
            Long value = redis.template().execute(SCRIPT, List.of("talkspace:rate:" + group + ":" + hash), String.valueOf(limit), String.valueOf(seconds));
            return value == null ? 0 : value;
        } catch (Exception ex) { redis.failure("rate_limit"); return 0; }
    }
}
