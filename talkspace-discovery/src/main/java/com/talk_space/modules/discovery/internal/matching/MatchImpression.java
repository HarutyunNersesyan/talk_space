package com.talk_space.modules.discovery.internal.matching;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;
@Entity
@Table(name = "match_impressions", uniqueConstraints = @UniqueConstraint(columnNames = {"viewer", "candidate", "mode"}),
 indexes = @Index(name = "idx_match_history", columnList = "viewer,mode,shown_at"))
@Getter @NoArgsConstructor
public class MatchImpression {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 20) private String viewer;
    @Column(nullable = false, length = 20) private String candidate;
    @Column(nullable = false, length = 20) private String mode;
    @Column(name = "shown_at", nullable = false) private Instant shownAt;
    @Column(name = "skipped_at") private Instant skippedAt;
}
