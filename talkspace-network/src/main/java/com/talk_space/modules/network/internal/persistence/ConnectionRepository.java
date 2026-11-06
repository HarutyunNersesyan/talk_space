package com.talk_space.modules.network.internal.persistence;
import com.talk_space.modules.network.api.Connection;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;
import java.util.Optional;
public interface ConnectionRepository extends JpaRepository<Connection, Long> {
    Optional<Connection> findByPairLowAndPairHigh(String low, String high);
    @Query("select c from Connection c where (c.sender = :actor or c.receiver = :actor) and not exists (select b from UserBlock b where (b.blocker=c.sender and b.blocked=c.receiver) or (b.blocker=c.receiver and b.blocked=c.sender))")
    Page<Connection> forActor(@Param("actor") String actor, Pageable page);
    long countByReceiverAndStatus(String receiver, Connection.State status);
    @Query(value = """
        select distinct case when c.sender=:userName then c.receiver else c.sender end
        from user_connections c
        where (c.sender=:userName or c.receiver=:userName) and c.status='ACCEPTED'
          and not exists (select 1 from user_blocks b
            where (b.blocker=c.sender and b.blocked=c.receiver)
               or (b.blocker=c.receiver and b.blocked=c.sender))
        """, nativeQuery = true)
    java.util.List<String> findConnectionPartnerNames(@Param("userName") String userName);
}
