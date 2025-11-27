package com.talk_space.service;

import com.talk_space.model.domain.ChatMessage;
import com.talk_space.model.domain.User;
import com.talk_space.model.dto.ChatMessageDto;
import com.talk_space.model.dto.UserChatDto;
import com.talk_space.repository.ChatRepository;
import com.talk_space.repository.UserRepository;
import com.talk_space.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public ChatMessage saveMessage(ChatMessage message) {
        logger.debug("Saving chat message from {} to {}",
                message.getSender().getUserName(), message.getReceiver().getUserName());
        return chatRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getChatHistory(String user1, String user2) {
        logger.debug("Getting chat history between {} and {}", user1, user2);
        List<ChatMessage> messages = chatRepository.findChatHistory(user1, user2);
        logger.info("Retrieved {} messages between {} and {}", messages.size(), user1, user2);
        return messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markMessagesAsRead(String sender, String receiver) {
        logger.debug("Marking messages as read from {} to {}", sender, receiver);
        chatRepository.markMessagesAsRead(sender, receiver);
        logger.info("Messages marked as read from {} to {}", sender, receiver);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String sender, String receiver) {
        logger.debug("Getting unread message count from {} to {}", sender, receiver);
        long count = chatRepository.countUnreadMessages(sender, receiver);
        logger.debug("Unread message count from {} to {}: {}", sender, receiver, count);
        return count;
    }

    @Transactional(readOnly = true)
    public List<UserChatDto> getUserChats(String userName) {
        logger.debug("Getting user chats for: {}", userName);

        // Debug logging
        System.out.println("=== DEBUG getUserChats for: " + userName);

        // Get all messages involving the user
        List<ChatMessage> allMessages = chatRepository.findAllUserMessages(userName);
        System.out.println("Found " + allMessages.size() + " messages");

        // Get mutual like partners to include newly created chats
        List<User> mutualLikePartners = userRepository.findMutualLikePartners(userName);
        System.out.println("Found " + mutualLikePartners.size() + " mutual like partners");

        mutualLikePartners.forEach(partner ->
                System.out.println("Mutual like partner: " + partner.getUserName())
        );

        Map<String, UserChatDto> chatMap = new HashMap<>();

        // Process existing messages
        for (ChatMessage message : allMessages) {
            String partner = message.getSender().getUserName().equals(userName)
                    ? message.getReceiver().getUserName()
                    : message.getSender().getUserName();

            if (!chatMap.containsKey(partner)) {
                User partnerUser = message.getSender().getUserName().equals(partner)
                        ? message.getSender()
                        : message.getReceiver();

                long unreadCount = chatRepository.countUnreadMessages(partner, userName);
                UserChatDto dto = buildUserChatDto(partnerUser, message, unreadCount);
                chatMap.put(partner, dto);
            } else {
                // Update with latest message if this one is newer
                UserChatDto existingDto = chatMap.get(partner);
                if (message.getTimestamp().isAfter(existingDto.getLastMessageTime())) {
                    existingDto.setLastMessage(message.getContent());
                    existingDto.setLastMessageTime(message.getTimestamp());
                    existingDto.setUnreadCount(chatRepository.countUnreadMessages(partner, userName));
                }
            }
        }

        // Add mutual like partners who don't have messages yet
        for (User partnerUser : mutualLikePartners) {
            String partnerUsername = partnerUser.getUserName();
            if (!chatMap.containsKey(partnerUsername)) {
                // Create a chat DTO for mutual like partners with no messages
                UserChatDto dto = new UserChatDto();
                dto.setPartnerUsername(partnerUsername);
                dto.setPartnerName(partnerUser.getFirstName() + " " + partnerUser.getLastName());
                dto.setLastMessage("Start a conversation...");
                dto.setLastMessageTime(LocalDateTime.now());
                dto.setUnreadCount(0L);

                if (partnerUser.getImage() != null) {
                    dto.setPartnerImage(partnerUser.getImage().getFilePath());
                }

                chatMap.put(partnerUsername, dto);
                System.out.println("Added mutual like partner to chat list: " + partnerUsername);
            }
        }

        List<UserChatDto> result = chatMap.values().stream()
                .sorted((a, b) -> b.getLastMessageTime().compareTo(a.getLastMessageTime()))
                .collect(Collectors.toList());

        System.out.println("Total chats in result: " + result.size());
        logger.info("Retrieved {} chats for user: {}", result.size(), userName);
        return result;
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
        logger.debug("Converting chat message to DTO, ID: {}", message.getId());

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