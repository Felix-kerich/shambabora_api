package com.app.shambabora.modules.collaboration.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "direct_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirectMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "sender_id", nullable = false)
    private Long senderId;
    
    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;
    
    @Column(nullable = false, length = 2000)
    private String content;
    
    @Column(length = 500)
    private String imageUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType messageType = MessageType.TEXT;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus status = MessageStatus.SENT;
    
    @Column(name = "read_at")
    private Instant readAt;
    
    @CreationTimestamp
    private Instant createdAt;
    
    public enum MessageType {
        TEXT,
        IMAGE,
        FILE,
        LOCATION
    }
    
    public enum MessageStatus {
        SENT,
        DELIVERED,
        READ,
        FAILED
    }
}
