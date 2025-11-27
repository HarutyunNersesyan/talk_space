package com.talk_space.api.controller.publics;

import com.talk_space.model.domain.ChatMessage;
import com.talk_space.model.dto.ChatMessageDto;
import com.talk_space.model.dto.TypingNotificationDto;
import com.talk_space.repository.UserRepository;
import com.talk_space.service.ChatService;
import com.talk_space.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDto messageDto) {
        logger.info("Sending message from {} to {}", messageDto.getSender(), messageDto.getReceiver());

        try {
            var sender = userRepository.findUserByUserName(messageDto.getSender())
                    .orElseThrow(() -> {
                        logger.error("Sender not found: {}", messageDto.getSender());
                        return new RuntimeException("Sender not found");
                    });
            var receiver = userRepository.findUserByUserName(messageDto.getReceiver())
                    .orElseThrow(() -> {
                        logger.error("Receiver not found: {}", messageDto.getReceiver());
                        return new RuntimeException("Receiver not found");
                    });

            ChatMessage message = new ChatMessage();
            message.setSender(sender);
            message.setReceiver(receiver);
            message.setContent(messageDto.getContent());
            message.setTimestamp(LocalDateTime.now());
            message.setRead(false);

            ChatMessage savedMessage = chatService.saveMessage(message);
            ChatMessageDto responseDto = chatService.convertToDto(savedMessage);

            // Send to receiver's personal queue
            messagingTemplate.convertAndSendToUser(
                    messageDto.getReceiver(),
                    "/queue/messages",
                    responseDto
            );

            // Send to sender's personal queue
            messagingTemplate.convertAndSendToUser(
                    messageDto.getSender(),
                    "/queue/messages",
                    responseDto
            );

            // Send reload notification to recipient
            Map<String, String> reloadNotification = new HashMap<>();
            reloadNotification.put("type", "reload");
            reloadNotification.put("sender", messageDto.getSender());
            reloadNotification.put("receiver", messageDto.getReceiver());

            messagingTemplate.convertAndSendToUser(
                    messageDto.getReceiver(),
                    "/queue/notifications",
                    reloadNotification
            );

            LoggingUtil.logUserAction(messageDto.getSender(), "SEND_MESSAGE",
                    "To: " + messageDto.getReceiver() + ", Content: " + messageDto.getContent());
            logger.debug("Message sent successfully, ID: {}", savedMessage.getId());

        } catch (Exception e) {
            logger.error("Error sending message from {} to {}", messageDto.getSender(), messageDto.getReceiver(), e);
            throw e;
        }
    }

    @MessageMapping("/typing")
    public void sendTypingNotification(@Payload TypingNotificationDto typingDto) {
        logger.debug("Typing notification from {} to {}", typingDto.getSender(), typingDto.getReceiver());
        messagingTemplate.convertAndSendToUser(
                typingDto.getReceiver(),
                "/queue/typing",
                typingDto
        );
    }
}