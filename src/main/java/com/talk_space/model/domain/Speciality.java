package com.talk_space.model.domain;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "speciality")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Speciality {

    @Column(name = "speciality_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "parent_id")
    private Long parentId;
}
