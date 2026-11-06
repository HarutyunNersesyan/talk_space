package com.talk_space.modules.auth.internal.session;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
public interface RefreshSessionRepository extends JpaRepository<RefreshSession,String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from RefreshSession s where s.id=:id")
    Optional<RefreshSession> lock(@Param("id") String id);
}
