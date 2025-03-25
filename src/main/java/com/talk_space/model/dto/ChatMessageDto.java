package com.talk_space.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessageDto {
    private Long id;
    private String sender;
    private String senderName;
    private String receiver;
    private String receiverName;
    private String content;
    private LocalDateTime timestamp;
    private boolean isRead;
    private String senderImage;
    private String receiverImage;
}