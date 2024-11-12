package com.talk_space.model.domain;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "image", uniqueConstraints = @UniqueConstraint(columnNames = {"user_name", "url"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url", nullable = false)
    private String url;

    @ManyToOne
    @JoinColumn(name = "user_name", referencedColumnName = "user_name",nullable = false)
    private User user;



}
