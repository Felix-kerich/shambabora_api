package com.app.shambabora.modules.recordskeeping.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class ActivityDTO {
    private Long id;
    private Long userId;
    private String type; // PLANTING, WEEDING, HARVESTING, IRRIGATION, FERTILIZATION, PEST_CONTROL
    private String description;
    private Instant activityDate;
    private Instant createdAt;
} 