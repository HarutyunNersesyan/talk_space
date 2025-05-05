package com.talk_space.model.domain;


import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "hobby")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hobby {

    @Column(name = "hobby_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "parent_id")
    private Long parentId;


    public Hobby(String name, Long parentId) {
        this.name = name;
        this.parentId = parentId;
    }
}
