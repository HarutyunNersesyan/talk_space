package com.talk_space.modules.network.internal.cleanup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import java.util.Map;
@Service @RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class FeatureDataCleanup {
    private final NamedParameterJdbcTemplate jdbc;
    @org.springframework.core.annotation.Order(0)
    @org.springframework.context.event.EventListener
    public void remove(com.talk_space.modules.users.api.UserDeleting event) {
        String user=event.userName();
        var parameters=Map.of("user",user);
        jdbc.update("delete from user_blocks where blocker=:user or blocked=:user",parameters);
        jdbc.update("delete from user_connections where sender=:user or receiver=:user",parameters);
        for (String table:java.util.List.of("user_likes","likes_count")) {
            Boolean exists=jdbc.getJdbcTemplate().execute((org.springframework.jdbc.core.ConnectionCallback<Boolean>) c->{
                try (var tables=c.getMetaData().getTables(null,null,null,new String[]{"TABLE"})) {
                    while(tables.next()) if (table.equalsIgnoreCase(tables.getString("TABLE_NAME"))) return true;
                    return false;
                }
            });
            if (Boolean.TRUE.equals(exists)) jdbc.update(table.equals("user_likes")
                ? "delete from user_likes where liker=:user or liked=:user" : "delete from likes_count where user_name=:user",parameters);
        }
    }
}
