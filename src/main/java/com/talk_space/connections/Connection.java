package com.talk_space.connections;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
@Entity @Table(name = "user_connections", uniqueConstraints = @UniqueConstraint(columnNames = {"pair_low", "pair_high"}),
 indexes = {@Index(name="idx_connection_receiver", columnList="receiver,status"), @Index(name="idx_connection_sender", columnList="sender,status")})
@Getter @Setter @NoArgsConstructor
public class Connection {
    public enum State { PENDING, ACCEPTED, REJECTED, CANCELLED, REMOVED }
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Version private Long version;
    @Column(name="pair_low", nullable=false, length=20) private String pairLow;
    @Column(name="pair_high", nullable=false, length=20) private String pairHigh;
    @Column(nullable=false, length=20) private String sender;
    @Column(nullable=false, length=20) private String receiver;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private State status;
    @Column(nullable=false) private Instant createdAt;
    @Column(nullable=false) private Instant updatedAt;
}
