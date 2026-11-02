package com.talk_space.matching;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import static com.talk_space.matching.SimilarityCalculator.*;
@Repository
@RequiredArgsConstructor
public class MatchingRepository {
    private final NamedParameterJdbcTemplate jdbc;
    private final MatchingProperties properties;
    private final com.talk_space.infrastructure.RedisSupport redis;
    public Profile profile(String name) {
        return redis.cached("matching:" + name, Profile.class, java.time.Duration.ofSeconds(30),
            () -> profiles(List.of(name)).get(name));
    }

    public List<String> candidates(String viewer, MatchMode mode, Instant since, int limit) {
        // Table names come exclusively from the enum, never from request parameters.
        String table = mode == MatchMode.HOBBIES ? "hobby" : "speciality";
        String join = mode == MatchMode.HOBBIES ? "user_hobbies" : "user_specialities";
        String column = mode == MatchMode.HOBBIES ? "hobby_name" : "speciality_name";
        String id = mode == MatchMode.HOBBIES ? "hobby_id" : "speciality_id";
        Instant now = Instant.now();
        String sql = """
            SELECT u.user_name FROM user_entity u
            JOIN %s ut ON ut.user_name = u.user_name
            JOIN %s t ON t.name = ut.%s
            LEFT JOIN %s exact_trait ON exact_trait.user_name=:viewer AND exact_trait.%s=t.name
            WHERE u.user_name <> :viewer AND u.status = 'ACTIVE'
              AND u.role = 'USER' AND u.verify_mail = true
              AND NOT EXISTS (SELECT 1 FROM user_blocks b WHERE (b.blocker=:viewer AND b.blocked=u.user_name)
                  OR (b.blocker=u.user_name AND b.blocked=:viewer))
              AND NOT EXISTS (SELECT 1 FROM user_connections c WHERE (c.status='ACCEPTED'
                  OR (c.status='PENDING' AND c.updated_at > :pendingSince)
                  OR (c.status='REJECTED' AND c.updated_at > :rejectedSince)
                  OR (c.status IN ('CANCELLED','REMOVED') AND c.updated_at > :cancelledSince)) AND
                  ((c.sender=:viewer AND c.receiver=u.user_name) OR (c.receiver=:viewer AND c.sender=u.user_name)))
              AND NOT EXISTS (SELECT 1 FROM match_impressions i WHERE i.viewer = :viewer
                  AND i.candidate = u.user_name AND i.mode = :mode AND (i.shown_at > :since OR i.skipped_at > :skipSince))
              AND EXISTS (SELECT 1 FROM %s mt JOIN %s m ON m.name = mt.%s
                  WHERE mt.user_name = :viewer AND (m.%s = t.%s OR m.parent_id = t.parent_id
                    OR m.%s = t.parent_id OR m.parent_id = t.%s))
            GROUP BY u.user_name ORDER BY COUNT(DISTINCT exact_trait.%s) DESC,
                COUNT(DISTINCT t.%s) DESC, RANDOM() LIMIT :limit
            """.formatted(join, table, column, join, column, join, table, column, id, id, id, id, column, id);
        return jdbc.queryForList(sql, Map.of("viewer",viewer,"mode",mode.name(),"since",Timestamp.from(since),"limit",limit,
            "skipSince",Timestamp.from(now.minus(java.time.Duration.ofHours(properties.getSkipHours()))),
            "pendingSince",Timestamp.from(now.minus(java.time.Duration.ofHours(properties.getPendingHours()))),
            "rejectedSince",Timestamp.from(now.minus(java.time.Duration.ofDays(properties.getRejectedDays()))),
            "cancelledSince",Timestamp.from(now.minus(java.time.Duration.ofHours(properties.getCancelledHours())))),String.class);
    }
    public Map<String, Profile> profiles(Collection<String> names) {
        Map<String, List<Trait>> hobbies = traits(names, "hobby", "user_hobbies", "hobby_name", "hobby_id");
        Map<String, List<Trait>> specialities = traits(names, "speciality", "user_specialities", "speciality_name", "speciality_id");
        Map<String, Profile> result = new HashMap<>();
        names.forEach(n -> result.put(n, new Profile(hobbies.getOrDefault(n, List.of()), specialities.getOrDefault(n, List.of()))));
        return result;
    }
    private Map<String, List<Trait>> traits(Collection<String> names, String table, String join, String column, String id) {
        Map<String, List<Trait>> result = new HashMap<>();
        jdbc.query("SELECT ut.user_name,t." + id + " AS id,t.parent_id FROM " + join + " ut JOIN " + table
                + " t ON t.name = ut." + column + " WHERE ut.user_name IN (:names)", Map.of("names", names),
            (org.springframework.jdbc.core.RowCallbackHandler) rs -> result.computeIfAbsent(rs.getString("user_name"), k -> new ArrayList<>())
                .add(new Trait(rs.getLong("id"), rs.getObject("parent_id", Long.class))));
        return result;
    }
    public List<String> names(MatchMode mode, Collection<Long> ids) {
        if (ids.isEmpty()) return List.of();
        String table=mode==MatchMode.HOBBIES ? "hobby" : "speciality";
        String id=mode==MatchMode.HOBBIES ? "hobby_id" : "speciality_id";
        return jdbc.queryForList("select name from " + table + " where " + id + " in (:ids) order by name",
            Map.of("ids",ids),String.class);
    }
    public void restartCycle(String viewer, MatchMode mode) {
        jdbc.update("delete from match_impressions where viewer=:viewer and mode=:mode and (skipped_at is null or skipped_at <= :cutoff)",
            Map.of("viewer",viewer,"mode",mode.name(),"cutoff",Timestamp.from(Instant.now().minus(java.time.Duration.ofHours(properties.getSkipHours())))));
    }
    public boolean skip(String viewer, String candidate, MatchMode mode, Instant now) {
        // Repeated requests do not extend an active cooldown. A candidate must actually have been shown.
        int count=jdbc.update("update match_impressions set skipped_at=:now where viewer=:v and candidate=:c and mode=:m and (skipped_at is null or skipped_at <= :cutoff)",
            Map.of("now",Timestamp.from(now),"v",viewer,"c",candidate,"m",mode.name(),
                "cutoff",Timestamp.from(now.minus(java.time.Duration.ofHours(properties.getSkipHours())))));
        return count>0 || Boolean.TRUE.equals(jdbc.queryForObject(
            "select count(*) > 0 from match_impressions where viewer=:v and candidate=:c and mode=:m",
            Map.of("v",viewer,"c",candidate,"m",mode.name()),Boolean.class));
    }
    public void record(String viewer, String candidate, MatchMode mode, Instant now) {
        // MatchingService holds the viewer row lock across this update/insert and candidate selection.
        var params=Map.of("v",viewer,"c",candidate,"m",mode.name(),"t",Timestamp.from(now));
        int updated=jdbc.update("update match_impressions set shown_at=:t, skipped_at=null where viewer=:v and candidate=:c and mode=:m",params);
        if (updated==0) jdbc.update("insert into match_impressions(viewer,candidate,mode,shown_at) values (:v,:c,:m,:t)",params);
    }
}
