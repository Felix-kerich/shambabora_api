package com.app.shambabora.modules.recordskeeping.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class YieldRecordResponse {
    private Long id;
    private Long patchId;
    private String patchName;
    private String cropType;
    private LocalDate harvestDate;
    private BigDecimal yieldAmount;
    private String unit;
    private BigDecimal areaHarvested;
    private BigDecimal yieldPerUnit;
    private BigDecimal marketPrice;
    private BigDecimal totalRevenue;
    private String qualityGrade;
    private String storageLocation;
    private String buyer;
    private String notes;
    private Long farmActivityId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
