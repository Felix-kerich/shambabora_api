package com.app.shambabora.modules.recordskeeping.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class YieldRecordRequest {
    @NotBlank
    private String cropType;

    @NotNull
    private LocalDate harvestDate;

    @NotNull
    @Positive
    private BigDecimal yieldAmount;

    @NotBlank
    private String unit;

    private BigDecimal areaHarvested;
    private BigDecimal marketPrice;
    private String qualityGrade;
    private String storageLocation;
    private String buyer;
    private String notes;
    private Long farmActivityId;
}
