package com.talk_space.repository;


import com.talk_space.model.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySenderUserName_UserNameAndReceiverUserName(String senderUserName, String receiverUserName);
}

