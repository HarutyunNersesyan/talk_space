package com.talk_space.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserChatDto {
    private String partnerUsername;
    private String partnerName;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private long unreadCount;
    private String partnerImage;
}
