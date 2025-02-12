package com.talk_space.model.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "image")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Image {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileType;

    @Column(nullable = false, unique = true)
    private String filePath;

    @ManyToOne
    @JoinColumn(name = "user_name", referencedColumnName = "user_name", nullable = false)
    private User user;
}

