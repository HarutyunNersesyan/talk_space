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
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        @Column(name = "user_with_chat_id", nullable = false)
        private Long chatUserId;


    public Chat(User user, Long chatUserId) {
        this.user = user;
        this.chatUserId = chatUserId;
    }
}
