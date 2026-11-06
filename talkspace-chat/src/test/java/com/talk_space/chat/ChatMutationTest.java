package com.talk_space.chat;

import com.talk_space.modules.chat.internal.persistence.ChatRepository;
import com.talk_space.modules.chat.api.ChatService;
import com.talk_space.modules.network.api.NetworkAccess;
import com.talk_space.modules.chat.internal.model.ChatMessage;
import com.talk_space.modules.users.api.model.User;
import com.talk_space.modules.users.internal.persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatMutationTest {
    @Mock ChatRepository repo;
    @Mock UserRepository users;
    @Mock NetworkAccess access;
    @InjectMocks ChatService service;
    ChatMessage message() {
        User alice = new User(); alice.setUserName("alice");
        User bob = new User(); bob.setUserName("bob");
        ChatMessage m = new ChatMessage(); m.setId(1L); m.setSender(alice); m.setReceiver(bob); m.setContent("hello"); return m;
    }
    @Test void senderCanEditAndReceiverCannot() {
        ChatMessage m = message(); when(repo.findForUpdate(1L)).thenReturn(Optional.of(m));
        assertTrue(service.edit(1L, "alice", "updated").isEdited());
        assertEquals("updated", m.getContent());
        assertEquals(403, assertThrows(ResponseStatusException.class, () -> service.edit(1L, "bob", "no")).getStatusCode().value());
    }
    @Test void localDeleteOnlyHidesFromActor() {
        ChatMessage m = message(); when(repo.findForUpdate(1L)).thenReturn(Optional.of(m));
        service.delete(1L, "bob", false);
        when(repo.findChatHistory("bob", "alice")).thenReturn(List.of(m));
        when(repo.findChatHistory("alice", "bob")).thenReturn(List.of(m));
        assertTrue(service.getChatHistory("bob", "alice").isEmpty());
        assertEquals(1, service.getChatHistory("alice", "bob").size());
    }
    @Test void onlySenderCanDeleteForEveryoneAndOutsidersCannotAccess() {
        ChatMessage m = message(); when(repo.findForUpdate(1L)).thenReturn(Optional.of(m));
        assertEquals(403, assertThrows(ResponseStatusException.class, () -> service.delete(1L, "bob", true)).getStatusCode().value());
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> service.delete(1L, "eve", false)).getStatusCode().value());
        service.delete(1L, "alice", true);
        assertTrue(m.isDeletedForSender()); assertTrue(m.isDeletedForReceiver());
    }
}
