package com.talk_space.search;
import com.talk_space.repository.UserRepository;
import com.talk_space.model.dto.SearchUser;
import com.talk_space.model.enums.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.*;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
@Service @RequiredArgsConstructor
public class UserSearchService {
    private final ElasticsearchGateway search;
    private final UserRepository users;
    private final MeterRegistry metrics;
    private final com.talk_space.blocks.UserBlockRepository blocks;
    public record Results(List<SearchUser> content, int page, int size, long indexedTotal) {}
    @Transactional(readOnly=true)
    public Results find(String actor, String query, int page, int size) {
        if (!search.enabled()) throw new ResponseStatusException(SERVICE_UNAVAILABLE, "Search is disabled; enable SEARCH_ENABLED");
        var textQuery = Map.of("multi_match", Map.of("query", query,
            "fields", List.of("userName.text^4", "firstName^3", "lastName^3", "hobbies^2", "specialities^2", "about"),
            "fuzziness", "AUTO"));
        Set<String> hidden=new HashSet<>(); hidden.add(actor);
        for (var b:blocks.involving(actor)) hidden.add(b.getBlocker().equals(actor)?b.getBlocked():b.getBlocker());
        var boolQuery = Map.of("must", List.of(textQuery),
            "must_not", List.of(Map.of("terms", Map.of("userName", hidden))));
        var request = Map.of("from", page * size, "size", size, "track_total_hits", true,
            "query", Map.of("bool", boolQuery),
            "sort", List.of(Map.of("_score", "desc"), Map.of("userName", "asc")));
        try {
            var result = search.request("POST", "/talkspace-users/_search", search.encode(request), "application/json");
            List<SearchUser> content = new ArrayList<>();
            for (var hit : result.path("hits").path("hits")) {
                users.findUserByUserName(hit.path("_source").path("userName").asText())
                    .filter(u -> !hidden.contains(u.getUserName()) && !blocks.blockedEitherWay(actor,u.getUserName()))
                    .filter(u -> u.getStatus() == Status.ACTIVE && u.getRole() == Role.USER && Boolean.TRUE.equals(u.getVerifyMail()))
                    .ifPresent(u -> content.add(new SearchUser(u)));
            }
            metrics.counter("talkspace.search.requests", "result", "success").increment();
            return new Results(content, page, size, result.path("hits").path("total").path("value").asLong());
        } catch (RuntimeException ex) {
            metrics.counter("talkspace.search.requests", "result", "failure").increment();
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, "Search is temporarily unavailable");
        }
    }
}
