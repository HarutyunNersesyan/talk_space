package com.talk_space.api.controller.publics;

import com.talk_space.model.domain.ChatMessage;
import com.talk_space.model.dto.ChatMessageDto;
import com.talk_space.model.dto.TypingNotificationDto;
import com.talk_space.repository.UserRepository;
import com.talk_space.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDto messageDto) {
        var sender = userRepository.findUserByUserName(messageDto.getSender())
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        var receiver = userRepository.findUserByUserName(messageDto.getReceiver())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(messageDto.getContent());
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false);

        ChatMessage savedMessage = chatService.saveMessage(message);
        ChatMessageDto responseDto = chatService.convertToDto(savedMessage);

        // Send to receiver
        messagingTemplate.convertAndSendToUser(
                messageDto.getReceiver(),
                "/queue/messages",
                responseDto
        );

        // Send back to sender (for sync across devices)
        messagingTemplate.convertAndSendToUser(
                messageDto.getSender(),
                "/queue/messages",
                responseDto
        );
    }

    @MessageMapping("/typing")
    public void sendTypingNotification(@Payload TypingNotificationDto typingDto) {
        // Forward typing notification to recipient
        messagingTemplate.convertAndSendToUser(
                typingDto.getReceiver(),
                "/queue/typing",
                typingDto
        );
    }
}