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
public class InputUsageRecordDTO {
    private Long id;
    private Long farmActivityId;
    private Long patchId;
    private String patchName;
    private Long yieldRecordId;
    private String inputType; // SEED, FERTILIZER, PESTICIDE, etc
    private Long seedVarietyId;
    private Long fertilizerProductId;
    private Long pesticideProductId;
    private BigDecimal quantityUsed;
    private String unit;
    private LocalDate applicationDate;
    private BigDecimal costOfUsage;
    private BigDecimal applicationRate;
    private Integer effectivenessRating;
    private String visibleResults;
    private Boolean contributedToHighYield;
    private Integer estimatedYieldContributionPercent;
    private String notes;
    private String problemsEncountered;
}
