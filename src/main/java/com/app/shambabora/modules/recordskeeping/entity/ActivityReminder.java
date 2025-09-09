package com.app.shambabora.modules.recordskeeping.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_reminders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityReminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "farm_activity_id", nullable = false)
    private Long farmActivityId;

    @Column(nullable = false)
    private LocalDateTime reminderDateTime;

    @Column(nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepeatInterval repeatInterval = RepeatInterval.NONE;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum RepeatInterval {
        NONE,
        DAILY,
        WEEKLY,
        MONTHLY
    }
} 