package com.app.shambabora.modules.recordskeeping.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityReminderRequest {
    @NotNull
    @Future
    private LocalDateTime reminderDateTime;

    @NotBlank
    private String message;

    private String repeatInterval; // NONE, DAILY, WEEKLY, MONTHLY
} 