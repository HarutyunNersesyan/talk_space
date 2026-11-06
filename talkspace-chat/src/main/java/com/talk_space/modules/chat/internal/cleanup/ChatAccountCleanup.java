package com.talk_space.modules.chat.internal.cleanup;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import com.talk_space.modules.users.api.UserDeleting;
import com.talk_space.modules.chat.internal.persistence.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
@Component @RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class ChatAccountCleanup {
    private final ChatRepository messages;
    @org.springframework.core.annotation.Order(0)
    @EventListener public void remove(UserDeleting event) {
        messages.deleteAll(messages.findSenderChatHistory(event.userName()));
        messages.deleteAll(messages.findReceiverChatHistory(event.userName()));
        messages.flush();
    }
}
