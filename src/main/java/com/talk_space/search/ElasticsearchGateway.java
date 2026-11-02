package com.talk_space.search;
import com.fasterxml.jackson.databind.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
@Component
public class ElasticsearchGateway {
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    private final ObjectMapper json;
    private final String base;
    private final boolean enabled;
    public ElasticsearchGateway(ObjectMapper json, @Value("${app.search.url:http://localhost:9200}") String base,
        @Value("${app.search.enabled:false}") boolean enabled) {
        this.json=json; this.base=base.replaceAll("/+$", ""); this.enabled=enabled;
    }
    public boolean enabled() { return enabled; }
    public JsonNode request(String method, String path, String body, String contentType) {
        try {
            var builder = HttpRequest.newBuilder(URI.create(base + path)).timeout(Duration.ofSeconds(20))
                .header("Content-Type", contentType).method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
            var response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300)
                throw new IllegalStateException("Elasticsearch HTTP " + response.statusCode());
            return json.readTree(response.body());
        } catch (InterruptedException ex) { Thread.currentThread().interrupt(); throw new IllegalStateException("Search interrupted", ex); }
        catch (java.io.IOException ex) { throw new IllegalStateException("Search unavailable", ex); }
    }
    public String encode(Object value) {
        try { return json.writeValueAsString(value); }
        catch (com.fasterxml.jackson.core.JsonProcessingException ex) { throw new IllegalArgumentException(ex); }
    }
    public void ensureIndex() {
        // Idempotent, including when multiple instances start together.
        try {
            request("PUT", "/talkspace-users", """
                {"mappings":{"properties":{
                  "userName":{"type":"keyword","fields":{"text":{"type":"text"}}},
                  "firstName":{"type":"text"},"lastName":{"type":"text"},"about":{"type":"text"},
                  "hobbies":{"type":"text"},"specialities":{"type":"text"},"generation":{"type":"keyword"}
                }}}
                """, "application/json");
        } catch (IllegalStateException ex) {
            // A successful GET distinguishes an existing index from connection/permission failures.
            request("GET", "/talkspace-users", null, "application/json");
        }
    }
}
