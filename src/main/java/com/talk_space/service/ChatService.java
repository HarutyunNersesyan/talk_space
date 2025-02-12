package com.talk_space.service;



import com.talk_space.model.domain.ChatMessage;
import com.talk_space.model.domain.User;
import com.talk_space.repository.ChatMessageRepository;
import com.talk_space.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;



    public ChatMessage saveMessage(String senderUserName, String receiverUserName, String content) {
        Optional<User> sender = userRepository.findUserByUserName(senderUserName);

        if (sender.isEmpty()) {
            throw new RuntimeException("Sender not found: " + senderUserName);
        }

        ChatMessage chatMessage = new ChatMessage(sender.get(), receiverUserName, content, LocalDateTime.now());
        return chatMessageRepository.save(chatMessage);
    }

    public List<ChatMessage> getChatHistory(String senderUserName, String receiverUserName) {
        return chatMessageRepository.findBySenderUserName_UserNameAndReceiverUserName(senderUserName, receiverUserName);
    }
}


