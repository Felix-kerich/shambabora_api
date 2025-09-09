package com.app.shambabora.modules.collaboration.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class MessageDTO {
    private Long id;
    private Long groupId;
    private Long senderId;
    private String content;
    private Instant sentAt;
} 