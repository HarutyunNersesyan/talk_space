package com.talk_space.api.controller.publics;

import com.talk_space.model.dto.ChatMessageDto;
import com.talk_space.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/chat")
@RequiredArgsConstructor
public class ChatApiController {
    private final ChatService chatService;

    @GetMapping("/history/{user1}/{user2}")
    public ResponseEntity<List<ChatMessageDto>> getChatHistory(
            @PathVariable String user1,
            @PathVariable String user2) {
        return ResponseEntity.ok(chatService.getChatHistory(user1, user2));
    }

    @PostMapping("/read/{sender}/{receiver}")
    public ResponseEntity<Void> markMessagesAsRead(
            @PathVariable String sender,
            @PathVariable String receiver) {
        chatService.markMessagesAsRead(sender, receiver);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread/{sender}/{receiver}")
    public ResponseEntity<Long> getUnreadCount(
            @PathVariable String sender,
            @PathVariable String receiver) {
        return ResponseEntity.ok(chatService.getUnreadCount(sender, receiver));
    }
}