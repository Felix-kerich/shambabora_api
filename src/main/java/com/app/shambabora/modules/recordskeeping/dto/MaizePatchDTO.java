package com.app.shambabora.modules.recordskeeping.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MaizePatchDTO {
    private Long id;
    private Long farmerProfileId;
    private Integer year;
    private String season;
    private String name;
    private String cropType;
    private Double area;
    private String areaUnit;
    private LocalDate plantingDate;
    private LocalDate expectedHarvestDate;
    private LocalDate actualHarvestDate;
    private String location;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Related records for the patch
    private List<FarmActivityResponse> activities;
    private List<YieldRecordResponse> yields;
    private List<FarmExpenseResponse> expenses;
}
