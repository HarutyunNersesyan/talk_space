package com.talk_space.model.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_likes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "liked_user_id", nullable = false)
    private Long likedUserId;
}
