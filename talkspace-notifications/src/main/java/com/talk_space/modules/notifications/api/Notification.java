package com.talk_space.modules.notifications.api;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
@Entity @Table(name="user_notifications", indexes={
 @Index(name="idx_notification_recipient_time", columnList="recipient,created_at"),
 @Index(name="idx_notification_unread", columnList="recipient,read_at")})
@Getter @Setter @NoArgsConstructor
public class Notification {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, length=20) private String recipient;
    @Column(nullable=false, length=20) private String actor;
    @Column(nullable=false, length=40) private String type;
    private Long connectionId;
    @Column(name="created_at", nullable=false) private Instant createdAt;
    @Column(name="read_at") private Instant readAt;
}
