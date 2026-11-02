package com.talk_space.api.controller.publics;

import com.talk_space.model.dto.ChatMessageDto;
import com.talk_space.model.dto.TypingNotificationDto;
import com.talk_space.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;
import java.security.Principal;
import java.util.Map;

@Controller @RequiredArgsConstructor
public class ChatController {
    private final ChatService chat;
    private final SimpMessagingTemplate websocket;
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDto payload, Principal principal) {
        String sender=actor(principal);
        ChatMessageDto saved=chat.send(sender,payload.getReceiver(),payload.getContent());
        websocket.convertAndSendToUser(saved.getReceiver(),"/queue/messages",saved);
        websocket.convertAndSendToUser(sender,"/queue/messages",saved);
    }
    @MessageMapping("/typing")
    public void sendTypingNotification(@Payload TypingNotificationDto payload, Principal principal) {
        String sender=actor(principal);
        chat.requireChat(sender,payload.getReceiver());
        payload.setSender(sender);
        websocket.convertAndSendToUser(payload.getReceiver(),"/queue/typing",payload);
    }
    private String actor(Principal principal) {
        if (principal==null) throw new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED);
        return principal.getName();
    }
    @MessageExceptionHandler(Exception.class)
    @SendToUser(value="/queue/errors",broadcast=false)
    public Map<String,String> error(Exception ex) {
        return Map.of("message",ex instanceof ResponseStatusException r && r.getReason()!=null ? r.getReason() : "Message could not be delivered. Check your connection status.");
    }
}
