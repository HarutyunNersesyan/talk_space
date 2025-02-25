package com.talk_space.service;



import com.talk_space.model.domain.ChatMessage;
import com.talk_space.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatMessageService {
    @Autowired
    private ChatMessageRepository repository;

    public ChatMessage saveMessage(ChatMessage message) {
        message.setTimestamp(LocalDateTime.now());
        return repository.save(message);
    }

    public List<ChatMessage> getMessagesForUser(String receiverUserName) {
        return repository.findByReceiverUserName(receiverUserName);
    }
}


