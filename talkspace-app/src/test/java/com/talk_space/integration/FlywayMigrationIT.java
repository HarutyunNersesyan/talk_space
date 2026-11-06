package com.talk_space.integration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.sql.DriverManager;
import static org.junit.jupiter.api.Assertions.*;

/** Uses a disposable PostgreSQL instance; never runs against the user's database. */
@Testcontainers
class FlywayMigrationIT {
    @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
    @Test void legacySchemaIsBaselinedWithoutDeletingExistingMessages() throws Exception {
        try (var connection=DriverManager.getConnection(postgres.getJdbcUrl(),postgres.getUsername(),postgres.getPassword());
             var sql=connection.createStatement()) {
            sql.execute("create table chat_messages (id bigint primary key, sender_user_id bigint not null, content text not null)");
            sql.execute("insert into chat_messages values (1, 10, 'existing message')");
        }
        Flyway flyway=Flyway.configure().dataSource(postgres.getJdbcUrl(),postgres.getUsername(),postgres.getPassword())
            .baselineOnMigrate(true).baselineVersion("1").locations("classpath:db/migration").load();
        flyway.migrate(); flyway.migrate();
        try (var connection=DriverManager.getConnection(postgres.getJdbcUrl(),postgres.getUsername(),postgres.getPassword());
             var sql=connection.createStatement()) {
            try (var result=sql.executeQuery("select content, client_message_id from chat_messages where id=1")) {
                assertTrue(result.next()); assertEquals("existing message",result.getString(1)); assertNull(result.getString(2));
            }
            sql.execute("insert into chat_messages values (2, 10, 'one', '550e8400-e29b-41d4-a716-446655440000')");
            assertThrows(java.sql.SQLException.class,()->sql.execute("insert into chat_messages values (3, 10, 'two', '550e8400-e29b-41d4-a716-446655440000')"));
        }
    }
}
