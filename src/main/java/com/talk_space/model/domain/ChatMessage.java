package com.talk_space.model.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_user_name", referencedColumnName = "user_name", nullable = false)
    private User senderUserName;

    @Column(name = "receiver_user_name", nullable = false)
    private String receiverUserName;

    private String content;

    private LocalDateTime timestamp;


    public ChatMessage(User senderUserName, String receiverUserName, String content, LocalDateTime timestamp) {
        this.senderUserName = senderUserName;
        this.receiverUserName = receiverUserName;
        this.content = content;
        this.timestamp = timestamp;
    }
}



