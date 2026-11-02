package com.talk_space.connections;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;
import java.util.Optional;
public interface ConnectionRepository extends JpaRepository<Connection, Long> {
    Optional<Connection> findByPairLowAndPairHigh(String low, String high);
    @Query("select c from Connection c where (c.sender = :actor or c.receiver = :actor) and not exists (select b from UserBlock b where (b.blocker=c.sender and b.blocked=c.receiver) or (b.blocker=c.receiver and b.blocked=c.sender))")
    Page<Connection> forActor(@Param("actor") String actor, Pageable page);
    long countByReceiverAndStatus(String receiver, Connection.State status);
}
