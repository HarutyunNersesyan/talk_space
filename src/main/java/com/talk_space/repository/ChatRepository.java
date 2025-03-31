package com.talk_space.repository;

import com.talk_space.model.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<ChatMessage, Long> {

    // Get all messages involving the user (either as sender or receiver)
    @Query("SELECT m FROM ChatMessage m WHERE " +
            "m.sender.userName = :userName OR m.receiver.userName = :userName " +
            "ORDER BY m.timestamp DESC")
    List<ChatMessage> findAllUserMessages(@Param("userName") String userName);

    // Get conversation history between two users
    @Query("SELECT m FROM ChatMessage m WHERE " +
            "(m.sender.userName = :user1 AND m.receiver.userName = :user2) OR " +
            "(m.sender.userName = :user2 AND m.receiver.userName = :user1) " +
            "ORDER BY m.timestamp")
    List<ChatMessage> findChatHistory(@Param("user1") String user1,
                                      @Param("user2") String user2);

    // Mark messages as read
    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE " +
            "m.sender.userName = :sender AND m.receiver.userName = :receiver AND m.isRead = false")
    void markMessagesAsRead(@Param("sender") String sender,
                            @Param("receiver") String receiver);

    // Count unread messages from a specific sender
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE " +
            "m.sender.userName = :sender AND m.receiver.userName = :receiver AND m.isRead = false")
    long countUnreadMessages(@Param("sender") String sender,
                             @Param("receiver") String receiver);
}