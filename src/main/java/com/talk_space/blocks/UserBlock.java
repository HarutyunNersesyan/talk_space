package com.talk_space.blocks;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name="user_blocks", uniqueConstraints=@UniqueConstraint(columnNames={"blocker", "blocked"}),
    indexes={@Index(name="idx_blocks_owner", columnList="blocker"), @Index(name="idx_blocks_target", columnList="blocked")})
@Getter @Setter @NoArgsConstructor
public class UserBlock {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, length=20) private String blocker;
    @Column(nullable=false, length=20) private String blocked;
    @Column(nullable=false) private Instant createdAt;
}
