package com.talk_space.model.domain;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "social_networks")
public class SocialNetworks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String platform;

    @Column(nullable = false)
    private String url;

    @ManyToOne
    @JoinColumn(name = "user_name", referencedColumnName = "user_name",nullable = false)
    private User user;
}
