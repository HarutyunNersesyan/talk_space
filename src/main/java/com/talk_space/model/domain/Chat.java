package com.talk_space.model.domain;


import jakarta.persistence.*;
import lombok.*;

@Entity
    @Table(name = "user_chats")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class Chat {

        @Column(name = "chat_id")
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "user_name", nullable = false)
        private User user;

        @Column(name = "user_name_with_chat", nullable = false)
        private String userName;


    public Chat(User user, String userName) {
        this.user = user;
        this.userName = userName;
    }
}
