package com.talk_space.model.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "review")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message", nullable = false, length = 200)
    private String message;

    @Column(name = "sender_user_name", nullable = false)
    private String senderUserName;


    @Column(name = "review_date")
    private LocalDate reviewDate;
    public Review(String message, String senderUserName, LocalDate reviewDate) {
        this.message = message;
        this.senderUserName = senderUserName;
        this.reviewDate = reviewDate;

    }
}
