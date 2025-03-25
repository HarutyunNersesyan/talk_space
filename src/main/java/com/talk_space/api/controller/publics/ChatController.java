package com.talk_space.api.controller.publics;


import com.talk_space.model.domain.ChatMessage;
import com.talk_space.model.dto.ChatMessageDto;
import com.talk_space.repository.UserRepository;
import com.talk_space.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

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

        ChatMessage savedMessage = chatService.saveMessage(message);
        ChatMessageDto responseDto = chatService.convertToDto(savedMessage);

        // Send to receiver
        messagingTemplate.convertAndSendToUser(
                receiver.getUserName(),
                "/queue/messages",
                responseDto
        );

        // Also send back to sender for sync
        messagingTemplate.convertAndSendToUser(
                sender.getUserName(),
                "/queue/messages",
                responseDto
        );
    }
}
