package com.talk_space.modules.discovery.internal.search;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.*;
@Service @RequiredArgsConstructor
public class SearchIndexer {
    private static final Logger log = LoggerFactory.getLogger(SearchIndexer.class);
    private final ElasticsearchGateway search;
    private final NamedParameterJdbcTemplate jdbc;
    private final MeterRegistry metrics;
    @Scheduled(initialDelayString="${app.search.initial-delay-ms:10000}", fixedDelayString="${app.search.sync-delay-ms:60000}")
    @Transactional
    public void synchronize() {
        if (!search.enabled()) return;
        // Transaction-scoped distributed lock: one index writer across backend instances.
        Boolean locked = jdbc.queryForObject("select pg_try_advisory_xact_lock(74823191)", Map.of(), Boolean.class);
        if (!Boolean.TRUE.equals(locked)) return;
        try {
            search.ensureIndex();
            String generation = UUID.randomUUID().toString(); long after = 0;
            while (true) {
                var rows = jdbc.queryForList("""
                    select u.user_id,u.user_name,u.first_name,u.last_name,coalesce(u.about_me,'') as about,
                      coalesce((select string_agg(hobby_name,' ') from user_hobbies where user_name=u.user_name),'') as hobbies,
                      coalesce((select string_agg(speciality_name,' ') from user_specialities where user_name=u.user_name),'') as specialities
                    from user_entity u where u.user_id > :after and u.status='ACTIVE' and u.role='USER'
                      and u.verify_mail=true order by u.user_id limit 500
                    """, Map.of("after", after));
                if (rows.isEmpty()) break;
                StringBuilder bulk = new StringBuilder();
                for (var row : rows) {
                    String name = (String) row.get("user_name"); after = ((Number)row.get("user_id")).longValue();
                    bulk.append(search.encode(Map.of("index", Map.of("_id", name)))).append('\n');
                    bulk.append(search.encode(Map.of("userName", name, "firstName", row.get("first_name"),
                        "lastName", row.get("last_name"), "about", row.get("about"), "hobbies", row.get("hobbies").toString().replace('_', ' '),
                        "specialities", row.get("specialities").toString().replace('_', ' '), "generation", generation))).append('\n');
                }
                var result = search.request("POST", "/talkspace-users/_bulk?refresh=wait_for", bulk.toString(), "application/x-ndjson");
                if (result.path("errors").asBoolean()) throw new IllegalStateException("Elasticsearch bulk item failed");
            }
            // Delete removed/blocked users only after every page has indexed successfully.
            search.request("POST", "/talkspace-users/_delete_by_query?refresh=true", search.encode(
                Map.of("query", Map.of("bool", Map.of("must_not", List.of(Map.of("term", Map.of("generation", generation))))))), "application/json");
            metrics.counter("talkspace.search.sync", "result", "success").increment();
        } catch (RuntimeException ex) {
            metrics.counter("talkspace.search.sync", "result", "failure").increment();
            log.warn("Search index synchronization failed: {}", ex.getMessage());
        }
    }
}
