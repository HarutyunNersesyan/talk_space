package com.talk_space.repository;


import com.talk_space.model.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByReceiverUserName(String receiverUserName);
}


