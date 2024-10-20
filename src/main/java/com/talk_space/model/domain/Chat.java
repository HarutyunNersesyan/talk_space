package com.talk_space.model.domain;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_chats")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_id")
    private Long id;


    @ManyToOne
    @JoinColumn(name = "user_name", referencedColumnName = "user_name", nullable = false)
    private User user;

    @Column(name = "user_name_with_chat", nullable = false)
    private String userNameWithChat;

    public Chat(User user, String userNameWithChat) {
        this.user = user;
        this.userNameWithChat = userNameWithChat;
    }
}
