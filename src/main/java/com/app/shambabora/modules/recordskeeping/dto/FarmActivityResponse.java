package com.app.shambabora.modules.recordskeeping.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class FarmActivityResponse {
    private Long id;
    private Long patchId;
    private String patchName;
    private String activityType;
    private String cropType;
    private LocalDate activityDate;
    private String description;
    private Double areaSize;
    private String units;
    private Double yield;
    private BigDecimal cost;
    private String productUsed;
    private Double applicationRate;
    
    // Enhanced fields
    private String weatherConditions;
    private String soilConditions;
    private String notes;
    private String location;
    private Integer laborHours;
    private String equipmentUsed;
    private BigDecimal laborCost;
    private BigDecimal equipmentCost;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String yieldTrend; // INCREASE, DECREASE, STEADY
    private Double percentageChange;
    private String possibleReasons;
}
