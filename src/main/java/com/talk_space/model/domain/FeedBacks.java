package com.talk_space.model.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "feed_backs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedBacks {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message", nullable = false, length = 200)
    private String message;

    @Column(name = "sender_user_name", nullable = false)
    private String senderUserName;

    @Column(name = "rating")
    private Double rating;


    @Column(name = "review_date")
    private LocalDate reviewDate;

    public FeedBacks(String senderUserName, String message, LocalDate reviewDate, Double rating) {

        this.senderUserName = senderUserName;
        this.message = message;
        this.reviewDate = reviewDate;
        this.rating = rating;

    }
}
