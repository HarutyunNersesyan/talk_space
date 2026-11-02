package com.talk_space.model.domain;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages", uniqueConstraints = @UniqueConstraint(name = "uq_chat_sender_client_id", columnNames = {"sender_user_id", "client_message_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(
            name = "sender_user_id",
            referencedColumnName = "user_id",
            nullable = false
    )
    private User sender;

    @ManyToOne
    @JoinColumn(
            name = "receiver_user_id",
            referencedColumnName = "user_id",
            nullable = false
    )
    private User receiver;

    @Column(name = "client_message_id", length = 36)
    private String clientMessageId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private boolean isRead = false;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}