package com.talk_space.modules.chat.api.dto;



import lombok.Data;

@Data
public class TypingNotificationDto {
    private String sender;
    private String receiver;
    private boolean isTyping;
}
