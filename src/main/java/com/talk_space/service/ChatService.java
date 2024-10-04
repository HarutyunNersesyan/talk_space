package com.talk_space.service;

import com.talk_space.model.domain.Chat;
import com.talk_space.model.domain.User;
import com.talk_space.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ChatService {

    private final ChatRepository chatRepository;

    public List<Chat> getAllUserChats(User user) {
        return chatRepository.findChatsByUser(user);
    }


}
