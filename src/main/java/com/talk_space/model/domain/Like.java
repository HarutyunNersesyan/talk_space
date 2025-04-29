package com.talk_space.model.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;


@Entity
@Table(name = "user_likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"liker", "liked"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "liker", referencedColumnName = "user_name", nullable = false)
    private User liker;


    @ManyToOne
    @JoinColumn(name = "liked", referencedColumnName = "user_name", nullable = false)
    private User liked;

    @CreatedDate
    @Column(name = "like_date")
    private LocalDate likeDate;


    public Like(User liker, User liked) {
        this.liker = liker;
        this.liked = liked;
        this.likeDate = LocalDate.now();
    }
}


