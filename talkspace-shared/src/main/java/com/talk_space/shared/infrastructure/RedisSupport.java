package com.talk_space.shared.infrastructure;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.util.function.Supplier;
@Component
public class RedisSupport {
    private final StringRedisTemplate redis;
    private final ObjectMapper json;
    private final MeterRegistry metrics;
    private final boolean enabled;
    public RedisSupport(StringRedisTemplate redis, ObjectMapper json, MeterRegistry metrics,
        @Value("${app.redis.enabled:false}") boolean enabled) {
        this.redis = redis; this.json = json; this.metrics = metrics; this.enabled = enabled;
    }
    public boolean enabled() { return enabled; }
    public StringRedisTemplate template() { return redis; }
    public void failure(String operation) { metrics.counter("talkspace.redis.failures", "operation", operation).increment(); }
    public <T> T cached(String key, Class<T> type, Duration ttl, Supplier<T> loader) {
        if (enabled) try {
            String value = redis.opsForValue().get("talkspace:cache:" + key);
            if (value != null) {
                metrics.counter("talkspace.cache.requests", "result", "hit").increment();
                return json.readValue(value, type);
            }
        } catch (Exception ex) { failure("cache_read"); }
        metrics.counter("talkspace.cache.requests", "result", "miss").increment();
        T result = loader.get();
        if (enabled) try { redis.opsForValue().set("talkspace:cache:" + key, json.writeValueAsString(result), ttl); }
        catch (Exception ex) { failure("cache_write"); }
        return result;
    }
    public void evict(String key) {
        if (enabled) try { redis.delete("talkspace:cache:" + key); }
        catch (Exception ex) { failure("cache_evict"); }
    }
}
