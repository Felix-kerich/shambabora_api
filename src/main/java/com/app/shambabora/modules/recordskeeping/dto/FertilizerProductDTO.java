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
public class FertilizerProductDTO {
    private Long id;
    private String productName;
    private String productType; // ORGANIC, SYNTHETIC, MIXED
    private Integer nitrogenPercent;
    private Integer phosphorusPercent;
    private Integer potassiumPercent;
    private String cropTypes;
    private String supplier;
    private String sourceLocation;
    private BigDecimal costPerUnit;
    private String unit;
    private LocalDate purchaseDate;
    private Integer recommendedApplicationRate;
    private String applicationTiming;
    private Integer shelfLifeMonths;
    private Integer effectivenessRating;
    private String farmerNotes;
    private Integer timesUsed;
    private BigDecimal averageYieldIncrease;
    private Boolean isCostEffective;
    private BigDecimal costPerKgYield;
    private Boolean isActive;
}
