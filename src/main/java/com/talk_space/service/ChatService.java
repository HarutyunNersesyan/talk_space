package com.talk_space.service;

import com.talk_space.model.domain.ChatMessage;
import com.talk_space.model.dto.ChatMessageDto;
import com.talk_space.repository.ChatRepository;
import com.talk_space.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public ChatMessage saveMessage(ChatMessage message) {
        return chatRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getChatHistory(String user1, String user2) {
        List<ChatMessage> messages = chatRepository.findChatHistory(user1, user2);
        return messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markMessagesAsRead(String sender, String receiver) {
        chatRepository.markMessagesAsRead(sender, receiver);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String sender, String receiver) {
        return chatRepository.countUnreadMessages(sender, receiver);
    }

    public ChatMessageDto convertToDto(ChatMessage message) {
        ChatMessageDto dto = modelMapper.map(message, ChatMessageDto.class);
        dto.setSender(message.getSender().getUserName());
        dto.setSenderName(message.getSender().getFirstName() + " " + message.getSender().getLastName());
        dto.setReceiver(message.getReceiver().getUserName());
        dto.setReceiverName(message.getReceiver().getFirstName() + " " + message.getReceiver().getLastName());
        dto.setSenderImage(message.getSender().getImage().getFilePath());
        dto.setReceiverImage(message.getReceiver().getImage().getFilePath());
        return dto;
    }
}