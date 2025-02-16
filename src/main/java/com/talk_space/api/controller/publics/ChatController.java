package com.talk_space.api.controller.publics;

import com.talk_space.model.domain.ChatMessage;
import com.talk_space.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        ChatMessage savedMessage = chatService.saveMessage(
                chatMessage.getSenderUserName().getUserName(),
                chatMessage.getReceiverUserName(),
                chatMessage.getContent()
        );

        messagingTemplate.convertAndSendToUser(
                savedMessage.getReceiverUserName(), "/queue/messages", savedMessage);
    }
}