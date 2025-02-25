package com.talk_space.api.controller.publics;


import com.talk_space.model.domain.ChatMessage;
import com.talk_space.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {
    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat")
    public void sendMessage(@Payload ChatMessage message) {
        chatMessageService.saveMessage(message);
        messagingTemplate.convertAndSendToUser(
                message.getReceiverUserName(), "/queue/messages", message
        );
    }
}
