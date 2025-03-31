package com.talk_space.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessageDto {
    private Long id;
    private String sender;
    private String senderName;
    private String senderDisplayName;  // Combined first + last name
    private String senderImage;
    private String receiver;
    private String receiverDisplayName;  // Combined first + last name
    private String receiverImage;
    private String receiverName;
    private String content;
    private LocalDateTime timestamp;
    private boolean isRead;
}