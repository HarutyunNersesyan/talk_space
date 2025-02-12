package com.talk_space.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_user_name", referencedColumnName = "user_name", nullable = false)
    private User senderUserName;

    @Column(name = "receiver_user_name", nullable = false)
    private String receiverUserName;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public ChatMessage(User senderUserName, String receiverUserName, String content, LocalDateTime timestamp) {
        this.senderUserName = senderUserName;
        this.receiverUserName = receiverUserName;
        this.content = content;
        this.timestamp = timestamp;
    }
}


