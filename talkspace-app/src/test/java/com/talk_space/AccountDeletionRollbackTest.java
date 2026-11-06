package com.talk_space;

import com.talk_space.modules.auth.internal.session.RefreshSessionService;
import com.talk_space.modules.feedback.api.model.FeedBacks;
import com.talk_space.modules.feedback.internal.persistence.FeedBacksRepository;
import com.talk_space.modules.users.api.UserDeleting;
import com.talk_space.modules.users.api.UserService;
import com.talk_space.modules.users.api.dto.DeleteAccount;
import com.talk_space.modules.users.api.model.*;
import com.talk_space.modules.users.internal.persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.*;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.core.Ordered;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.LocalDate;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class AccountDeletionRollbackTest {
    @Autowired UserRepository users;
    @Autowired UserService accounts;
    @Autowired FeedBacksRepository feedback;
    @Autowired RefreshSessionService refresh;
    @Autowired PasswordEncoder encoder;
    @Autowired PlatformTransactionManager transactions;
    @Autowired JdbcTemplate jdbc;
    @Autowired @Qualifier("applicationEventMulticaster") ApplicationEventMulticaster events;

    @Test void failingModuleRollsBackAllDatabaseCleanup() {
        var tx = new TransactionTemplate(transactions);
        tx.executeWithoutResult(status -> {
            User u = new User();
            u.setFirstName("Alice"); u.setLastName("Smith"); u.setUserName("rollbackuser");
            u.setEmail("rollbackuser@gmail.com"); u.setBirthDate(LocalDate.of(2000, 1, 1));
            u.setGender(Gender.FEMALE); u.setRole(Role.USER); u.setStatus(Status.ACTIVE);
            u.setVerifyMail(true); u.setPassword(encoder.encode("DeletionPass1!"));
            u.setHobbies(new ArrayList<>()); u.setSpecialities(new ArrayList<>());
            users.saveAndFlush(u);
            feedback.saveAndFlush(new FeedBacks("rollbackuser", "must survive rollback", LocalDate.now(), 5.0));
            refresh.start("rollbackuser@gmail.com");
        });
        var failure = new FailingModule();
        events.addApplicationListener(failure);
        var request = new DeleteAccount("rollbackuser@gmail.com", "DeletionPass1!");
        try {
            assertThrows(IllegalStateException.class, () -> accounts.delete(request));
            assertTrue(failure.called, "Failure must occur after real cleanup listeners");
            assertEquals(1, count("user_entity", "user_name", "rollbackuser"));
            assertEquals(1, count("feed_backs", "sender_user_name", "rollbackuser"));
            assertEquals(1, count("refresh_sessions", "email", "rollbackuser@gmail.com"));
        } finally {
            events.removeApplicationListener(failure);
            accounts.delete(request);
        }
        assertEquals(0, count("user_entity", "user_name", "rollbackuser"));
        assertEquals(0, count("feed_backs", "sender_user_name", "rollbackuser"));
        assertEquals(0, count("refresh_sessions", "email", "rollbackuser@gmail.com"));
    }

    private int count(String table, String column, String value) {
        return jdbc.queryForObject("select count(*) from " + table + " where " + column + "=?", Integer.class, value);
    }

    private class FailingModule implements ApplicationListener<ApplicationEvent>, Ordered {
        boolean called;
        @Override public int getOrder() { return Ordered.LOWEST_PRECEDENCE; }
        @Override public void onApplicationEvent(ApplicationEvent event) {
            if (event instanceof PayloadApplicationEvent<?> payload
                    && payload.getPayload() instanceof UserDeleting deleting
                    && deleting.userName().equals("rollbackuser")) {
                assertEquals(0, count("feed_backs", "sender_user_name", "rollbackuser"));
                assertEquals(0, count("refresh_sessions", "email", "rollbackuser@gmail.com"));
                called = true;
                throw new IllegalStateException("Simulated module cleanup failure");
            }
        }
    }
}
