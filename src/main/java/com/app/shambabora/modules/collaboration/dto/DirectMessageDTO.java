package com.app.shambabora.modules.collaboration.dto;

import com.app.shambabora.modules.collaboration.entity.DirectMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirectMessageDTO {
    private Long id;
    private Long senderId;
    private String senderName;
    private Long recipientId;
    private String recipientName;
    private String content;
    private String imageUrl;
    private DirectMessage.MessageType messageType;
    private DirectMessage.MessageStatus status;
    private Instant readAt;
    private Instant createdAt;
}
