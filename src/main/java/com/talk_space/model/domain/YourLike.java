package com.talk_space.model.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_your_likes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class YourLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "your_liked_user_id", nullable = false)
    private Long yourLikedUserId;
}

