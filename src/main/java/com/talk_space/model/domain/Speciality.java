package com.talk_space.model.domain;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "speciality")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Speciality {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "user_name", referencedColumnName = "user_name",nullable = false)
    private User user;
}
