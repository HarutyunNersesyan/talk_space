package com.talk_space.repository;


import com.talk_space.model.domain.Chat;
import com.talk_space.model.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository  extends JpaRepository<Chat, Long> {

    List<Chat> findChatsByUser(User user);


}
