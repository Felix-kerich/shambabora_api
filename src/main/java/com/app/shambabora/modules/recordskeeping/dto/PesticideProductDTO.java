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
public class PesticideProductDTO {
    private Long id;
    private String productName;
    private String pesticideType; // INSECTICIDE, FUNGICIDE, etc
    private String pesticideClass; // Synthetic, Organic, Biological
    private String activeIngredient;
    private String concentrationPercent;
    private String targetPests;
    private String cropTypes;
    private String supplier;
    private String sourceLocation;
    private BigDecimal costPerUnit;
    private String unit;
    private LocalDate purchaseDate;
    private BigDecimal recommendedDosePerAcre;
    private String applicationMethod;
    private Integer daysToHarvestAfterApplication;
    private Integer effectiveDays;
    private Integer effectivenessRating;
    private String pestsControlled;
    private String environmentalImpact;
    private String farmerNotes;
    private Integer timesUsed;
    private String toxicityLevel;
    private String safetyPrecautions;
    private Boolean requiresPersonalProtection;
    private Boolean isActive;
    private Boolean isRecommendedOrganic;
}
