package com.talk_space.notifications;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;
import java.time.Instant;
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByRecipientOrderByCreatedAtDescIdDesc(String recipient, Pageable page);
    long countByRecipientAndReadAtIsNull(String recipient);
    @Modifying @Query("delete from Notification n where (n.recipient=:a and n.actor=:b) or (n.recipient=:b and n.actor=:a)")
    int deleteBetween(@Param("a") String a,@Param("b") String b);
    @Modifying @Query("update Notification n set n.readAt = :now where n.recipient = :recipient and n.readAt is null")
    int readAll(@Param("recipient") String recipient, @Param("now") Instant now);
}
