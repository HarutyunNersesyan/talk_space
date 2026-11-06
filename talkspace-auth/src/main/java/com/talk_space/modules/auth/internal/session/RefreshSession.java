package com.talk_space.modules.auth.internal.session;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
@Entity @Table(name="refresh_sessions") @Getter @Setter @NoArgsConstructor
public class RefreshSession {
    @Id private String id;
    @Column(nullable=false) private String email;
    @Column(nullable=false, length=64) private String tokenHash;
    @Column(nullable=false, length=64) private String passwordHash;
    @Column(nullable=false) private Instant expiresAt;
    @Column(nullable=false) private boolean revoked;
}
