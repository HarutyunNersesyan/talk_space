package com.talk_space.integration;
import com.talk_space.matching.*;
import com.talk_space.connections.*;
import com.talk_space.notifications.*;
import com.talk_space.infrastructure.*;
import com.talk_space.search.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.*;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.*;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest @ActiveProfiles("test") @Testcontainers
class InfrastructureIT {
    @Container static PostgreSQLContainer<?> postgres=new PostgreSQLContainer<>("postgres:16-alpine");
    @Container static GenericContainer<?> redis=new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);
    @Container static ElasticsearchContainer elastic=new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:8.15.3")
        .withEnv("xpack.security.enabled","false").withEnv("ES_JAVA_OPTS","-Xms512m -Xmx512m");
    @DynamicPropertySource static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",postgres::getJdbcUrl);registry.add("spring.datasource.username",postgres::getUsername);
        registry.add("spring.datasource.password",postgres::getPassword);registry.add("spring.datasource.driver-class-name",()->"org.postgresql.Driver");
        registry.add("spring.jpa.properties.hibernate.dialect",()->"org.hibernate.dialect.PostgreSQLDialect");
        registry.add("app.redis.enabled",()->true);registry.add("spring.data.redis.host",redis::getHost);
        registry.add("spring.data.redis.port",()->redis.getMappedPort(6379));
        registry.add("app.search.enabled",()->true);registry.add("app.search.url",()->"http://"+elastic.getHttpHostAddress());
        registry.add("app.search.initial-delay-ms",()->86400000);
    }
    @Autowired MatchingService matching;
    @Autowired MatchingRepository matchingRepository;
    @Autowired ConnectionService connections;
    @Autowired NotificationRepository notifications;
    @Autowired NamedParameterJdbcTemplate jdbc;
    @Autowired RateLimitService rateLimit;
    @Autowired PresenceService presence;
    @Autowired SearchIndexer indexer;
    @Autowired UserSearchService search;
    @Autowired TransactionTemplate transaction;
    @BeforeEach void seed() {
        jdbc.update("delete from user_blocks",Map.of());jdbc.update("delete from user_notifications",Map.of());jdbc.update("delete from user_connections",Map.of());
        jdbc.update("delete from match_impressions",Map.of());
        jdbc.update("delete from user_hobbies",Map.of());jdbc.update("delete from user_specialities",Map.of());
        jdbc.update("delete from user_entity",Map.of());jdbc.update("delete from hobby",Map.of());jdbc.update("delete from speciality",Map.of());
        jdbc.update("insert into hobby(hobby_id,name,parent_id) values (10,'Music',null),(11,'Guitar',10),(12,'Piano',10)",Map.of());
        for(String name:List.of("alice","bob","carol")) {
            jdbc.update("""
                insert into user_entity(first_name,last_name,user_name,birth_date,gender,email,password,role,status,verify_mail)
                values ('Alice','Smith',:name,'2000-01-01','FEMALE',:email,'hash','USER','ACTIVE',true)
                """,Map.of("name",name,"email",name+"@gmail.com"));
        }
        jdbc.update("insert into user_hobbies(user_name,hobby_name) values ('alice','Guitar'),('bob','Guitar'),('carol','Piano')",Map.of());
    }
    @Test void matchingRanksExactThenPartialAndRecyclesAfterFullCycle() {
        assertEquals("bob",matching.next("alice",MatchMode.HOBBIES).getUserName());
        assertEquals("carol",matching.next("alice",MatchMode.HOBBIES).getUserName());
        assertEquals("bob",matching.next("alice",MatchMode.HOBBIES).getUserName());
    }
    @Test void concurrentMatchingRequestsNeverReceiveSameCandidate() throws Exception {
        var pool=Executors.newFixedThreadPool(2);
        try {
            var results=pool.invokeAll(List.<Callable<String>>of(()->matching.next("alice",MatchMode.HOBBIES).getUserName(),
                ()->matching.next("alice",MatchMode.HOBBIES).getUserName()));
            assertNotEquals(results.get(0).get(),results.get(1).get());
        } finally {pool.shutdownNow();}
    }
    @Test void impressionUpsertIsUniqueAndBlocksExcludeCandidates() {
        transaction.executeWithoutResult(t->{matchingRepository.record("alice","bob",MatchMode.HOBBIES,Instant.now());
            matchingRepository.record("alice","bob",MatchMode.HOBBIES,Instant.now());});
        assertEquals(1,jdbc.queryForObject("select count(*) from match_impressions",Map.of(),Integer.class));
        jdbc.update("insert into user_blocks(blocker,blocked,created_at) values ('alice','carol',CURRENT_TIMESTAMP),('bob','alice',CURRENT_TIMESTAMP)",Map.of());
        assertThrows(RuntimeException.class,()->matching.next("alice",MatchMode.HOBBIES));
    }
    @Test void connectionWorkflowPersistsNotificationsAndIsIdempotent() {
        var request=connections.request("alice","bob");assertEquals(request.getId(),connections.request("alice","bob").getId());
        connections.transition("bob",request.getId(),"accept");connections.transition("bob",request.getId(),"accept");
        assertEquals(1,notifications.countByRecipientAndReadAtIsNull("bob"));
        assertEquals(1,notifications.countByRecipientAndReadAtIsNull("alice"));
    }
    @Test void redisRateLimitIsAtomicAndKeysExpire() throws Exception {
        String identity=UUID.randomUUID().toString();assertEquals(0,rateLimit.retryAfter(identity,"test",1,1));
        assertTrue(rateLimit.retryAfter(identity,"test",1,1)>0);
        Thread.sleep(1150);assertEquals(0,rateLimit.retryAfter(identity,"test",1,1));
    }
    @Test void redisPresenceRetainsOtherSessions() {
        presence.heartbeat("alice","a");presence.heartbeat("alice","b");presence.disconnect("alice","a");
        assertTrue(presence.online("alice"));presence.disconnect("alice","b");assertFalse(presence.online("alice"));
    }
    @Test void elasticsearchIndexesSearchesAndRemovesBlockedProfiles() {
        indexer.synchronize();var result=search.find("alice","Guitar",0,20);
        assertTrue(result.content().stream().anyMatch(u->u.getUserName().equals("bob")));
        jdbc.update("update user_entity set status='BLOCKED' where user_name='bob'",Map.of());
        assertTrue(search.find("alice","Guitar",0,20).content().stream().noneMatch(u->u.getUserName().equals("bob")));
        indexer.synchronize();assertTrue(search.find("alice","Guitar",0,20).content().isEmpty());
    }
}
