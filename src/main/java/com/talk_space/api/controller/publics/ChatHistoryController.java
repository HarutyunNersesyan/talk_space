package com.talk_space.api.controller.publics;

import com.talk_space.model.domain.ChatMessage;
import com.talk_space.service.ChatService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/chats")
public class ChatHistoryController {

    private final ChatService chatService;

    public ChatHistoryController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/{senderUserName}/{receiverUserName}")
    public List<ChatMessage> getChatHistory(@PathVariable String senderUserName, @PathVariable String receiverUserName) {
        return chatService.getChatHistory(senderUserName, receiverUserName);
    }
}



