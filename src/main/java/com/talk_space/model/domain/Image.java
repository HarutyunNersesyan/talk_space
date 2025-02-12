package com.talk_space.model.domain;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "image", uniqueConstraints = @UniqueConstraint(columnNames = {"user_name"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "data", nullable = false, columnDefinition = "BYTEA")
    private byte[] data;

    @ManyToOne
    @JoinColumn(name = "user_name", referencedColumnName = "user_name",nullable = false)
    private User user;
}

