package com.talk_space.service;

import com.talk_space.model.domain.ChatMessage;
import com.talk_space.model.domain.User;
import com.talk_space.model.dto.ChatMessageDto;
import com.talk_space.model.dto.UserChatDto;
import com.talk_space.repository.ChatRepository;
import com.talk_space.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Transactional(readOnly = true)
    public List<UserChatDto> getUserChats(String userName) {
        // Get all messages involving the user
        List<ChatMessage> allMessages = chatRepository.findAllUserMessages(userName);

        // Track latest message with each partner
        Map<String, ChatMessage> latestMessages = new HashMap<>();

        for (ChatMessage message : allMessages) {
            String partner = message.getSender().getUserName().equals(userName)
                    ? message.getReceiver().getUserName()
                    : message.getSender().getUserName();

            // Keep only the latest message per partner
            if (!latestMessages.containsKey(partner) ||
                    message.getTimestamp().isAfter(latestMessages.get(partner).getTimestamp())) {
                latestMessages.put(partner, message);
            }
        }

        // Convert to DTOs with unread counts
        return latestMessages.entrySet().stream()
                .map(entry -> {
                    String partner = entry.getKey();
                    ChatMessage lastMessage = entry.getValue();
                    User partnerUser = lastMessage.getSender().getUserName().equals(partner)
                            ? lastMessage.getSender()
                            : lastMessage.getReceiver();

                    long unreadCount = chatRepository.countUnreadMessages(partner, userName);

                    return buildUserChatDto(partnerUser, lastMessage, unreadCount);
                })
                .sorted((a, b) -> b.getLastMessageTime().compareTo(a.getLastMessageTime()))
                .collect(Collectors.toList());
    }

    private UserChatDto buildUserChatDto(User partnerUser, ChatMessage lastMessage, long unreadCount) {
        UserChatDto dto = new UserChatDto();
        dto.setPartnerUsername(partnerUser.getUserName());
        dto.setPartnerName(partnerUser.getFirstName() + " " + partnerUser.getLastName());
        dto.setLastMessage(lastMessage.getContent());
        dto.setLastMessageTime(lastMessage.getTimestamp());
        dto.setUnreadCount(unreadCount);

        if (partnerUser.getImage() != null) {
            dto.setPartnerImage(partnerUser.getImage().getFilePath());
        }

        return dto;
    }

    public ChatMessageDto convertToDto(ChatMessage message) {
        ChatMessageDto dto = new ChatMessageDto();

        // Manual mapping to avoid ModelMapper confusion
        dto.setId(message.getId());
        dto.setSender(message.getSender().getUserName());
        dto.setSenderDisplayName(message.getSender().getFirstName() + " " + message.getSender().getLastName());
        dto.setReceiver(message.getReceiver().getUserName());
        dto.setReceiverDisplayName(message.getReceiver().getFirstName() + " " + message.getReceiver().getLastName());
        dto.setContent(message.getContent());
        dto.setTimestamp(message.getTimestamp());
        dto.setRead(message.isRead());

        // Handle images if they exist
        if (message.getSender().getImage() != null) {
            dto.setSenderImage(message.getSender().getImage().getFilePath());
        }
        if (message.getReceiver().getImage() != null) {
            dto.setReceiverImage(message.getReceiver().getImage().getFilePath());
        }

        return dto;
    }
}