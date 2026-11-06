package com.talk_space.modules.network.internal.persistence;
import com.talk_space.modules.network.api.UserBlock;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;
import java.util.*;

public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {
    Optional<UserBlock> findByBlockerAndBlocked(String blocker, String blocked);
    Page<UserBlock> findByBlockerOrderByCreatedAtDescIdDesc(String blocker, Pageable page);
    @Query("select (count(b) > 0) from UserBlock b where (b.blocker=:a and b.blocked=:b) or (b.blocker=:b and b.blocked=:a)")
    boolean blockedEitherWay(@Param("a") String a, @Param("b") String b);
    @Query("select b from UserBlock b where b.blocker=:actor or b.blocked=:actor")
    List<UserBlock> involving(@Param("actor") String actor);
}
