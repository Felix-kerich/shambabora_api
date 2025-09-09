package com.app.shambabora.modules.recordskeeping.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FarmActivityRequest {
    @NotBlank
    private String activityType;

    @NotBlank
    private String cropType;

    @NotNull
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
}
