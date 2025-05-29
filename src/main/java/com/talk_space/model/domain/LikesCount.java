package com.talk_space.model.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "likes_count")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikesCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference
    @OneToOne
    @JoinColumn(name = "user_name", referencedColumnName = "user_name", unique = true)
    private User user;

    private Integer count;
}
