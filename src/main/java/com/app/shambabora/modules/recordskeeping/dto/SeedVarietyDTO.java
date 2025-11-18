package com.app.shambabora.modules.recordskeeping.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeedVarietyDTO {
    private Long id;
    private String cropType;
    private String varietyName;
    private String supplier;
    private String sourceLocation;
    private BigDecimal costPerUnit;
    private BigDecimal quantityBought;
    private String unit;
    private LocalDate purchaseDate;
    private Integer daysToMaturity;
    private String soilPreference;
    private String waterRequirement;
    private String diseaseResistance;
    private String pestResistance;
    private String climateAdaptation;
    private Integer yieldRating; // 1-5 stars
    private Integer easinessRating;
    private String farmerNotes;
    private Boolean isActive;
    private Integer timesUsed;
    private BigDecimal averageYieldPerUnit;
}
