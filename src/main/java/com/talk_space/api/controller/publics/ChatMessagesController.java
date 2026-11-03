package com.talk_space.api.controller.publics;

import com.talk_space.connections.ActorService;
import com.talk_space.model.dto.ChatMessageDto;
import com.talk_space.chat.service.ChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/public/chat") @RequiredArgsConstructor
public class ChatMessagesController {
    private static final Logger log = LoggerFactory.getLogger(ChatMessagesController.class);
    private final ChatService chatService;
    private final ActorService actors;
    private final SimpMessagingTemplate websocket;
    public record SendRequest(@NotBlank @Size(max=20) String receiver,
                              @NotBlank @Size(max=4000) String content,
                              @NotBlank @Pattern(regexp="(?i)^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$") String clientMessageId) {}
    @PostMapping("/messages")
    public ResponseEntity<ChatMessageDto> send(@Valid @RequestBody SendRequest request) {
        String sender = actors.currentMember().getUserName();
        ChatMessageDto saved = chatService.send(sender, request.receiver(), request.content(), request.clientMessageId());
        // The save committed before this best-effort notification. A retry is idempotent.
        try { websocket.convertAndSendToUser(saved.getReceiver(), "/queue/messages", saved);
            websocket.convertAndSendToUser(sender, "/queue/messages", saved); }
        catch (RuntimeException ex) { log.warn("Real-time chat notification failed; history remains available", ex); }
        return ResponseEntity.ok(saved);
    }
    public record EditRequest(@NotBlank @Size(max=4000) String content) {}
    @PatchMapping("/messages/{id}")
    public ResponseEntity<ChatMessageDto> edit(@PathVariable Long id, @Valid @RequestBody EditRequest request) {
        String actor = actors.currentMember().getUserName();
        ChatMessageDto saved = chatService.edit(id, actor, request.content());
        notifyChange(saved.getReceiver());
        notifyChange(actor);
        return ResponseEntity.ok(saved);
    }
    @DeleteMapping("/messages/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam(defaultValue="false") boolean everyone) {
        String actor = actors.currentMember().getUserName();
        String other = chatService.delete(id, actor, everyone);
        notifyChange(actor);
        if (everyone) notifyChange(other);
        return ResponseEntity.noContent().build();
    }
    private void notifyChange(String user) {
        try { websocket.convertAndSendToUser(user, "/queue/chat-changes", java.util.Map.of("changed", true)); }
        catch (RuntimeException ex) { log.warn("Real-time chat update failed; polling will recover", ex); }
    }
}
