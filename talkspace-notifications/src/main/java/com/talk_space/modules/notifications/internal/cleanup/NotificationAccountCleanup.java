package com.talk_space.modules.notifications.internal.cleanup;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import com.talk_space.modules.users.api.UserDeleting;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.util.Map;
@Component @RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class NotificationAccountCleanup {
    private final NamedParameterJdbcTemplate jdbc;
    @org.springframework.core.annotation.Order(0)
    @EventListener public void remove(UserDeleting event) { jdbc.update("delete from user_notifications where recipient=:name or actor=:name",Map.of("name",event.userName())); }
}
