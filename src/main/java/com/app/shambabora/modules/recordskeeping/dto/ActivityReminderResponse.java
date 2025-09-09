package com.app.shambabora.modules.recordskeeping.dto;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityReminderResponse {
    private Long id;
    private Long activityId;
    private LocalDateTime reminderDateTime;
    private String message;
    private String repeatInterval;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}