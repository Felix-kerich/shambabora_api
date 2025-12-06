package com.app.shambabora.modules.recordskeeping.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Data Transfer Object optimized for RAG (Retrieval-Augmented Generation) service.
 * Contains comprehensive patch data including activities, expenses, yields, and computed metrics.
 * This DTO is sent to the RAG service for AI analysis and recommendations.
 */
@Data
@Builder
public class PatchRagDataDTO {
    // Patch metadata
    private Long patchId;
    private Long farmerProfileId;
    private Integer year;
    private String season;
    private String patchName;
    private String cropType;
    private Double area;
    private String areaUnit;
    private LocalDate plantingDate;
    private LocalDate expectedHarvestDate;
    private LocalDate actualHarvestDate;
    private String location;
    private String notes;

    // Computed metrics
    private BigDecimal totalExpenses;
    private BigDecimal totalYield;
    private BigDecimal totalRevenue;
    private BigDecimal costPerKg;
    private BigDecimal profit;
    private BigDecimal profitPerKg;
    private BigDecimal roiPercentage;

    // Detailed records
    private List<ActivityDetailDTO> activities;
    private List<ExpenseDetailDTO> expenses;
    private List<YieldDetailDTO> yields;

    /**
     * Activity detail for RAG analysis
     */
    @Data
    @Builder
    public static class ActivityDetailDTO {
        private Long id;
        private String activityType;
        private LocalDate activityDate;
        private String description;
        private Double areaSize;
        private String units;
        private String productUsed;
        private Double applicationRate;
        private String weatherConditions;
        private String soilConditions;
        private Integer laborHours;
        private String equipmentUsed;
        private BigDecimal laborCost;
        private BigDecimal equipmentCost;
        private String notes;
    }

    /**
     * Expense detail for RAG analysis (seeds, fertilizers, pesticides, labour)
     */
    @Data
    @Builder
    public static class ExpenseDetailDTO {
        private Long id;
        private String category; // SEEDS, FERTILIZER, PESTICIDES, LABOR, EQUIPMENT, etc.
        private String description;
        private BigDecimal amount;
        private LocalDate expenseDate;
        private String supplier;
        private String growthStage; // When in the season was this expense incurred?
        private String notes;
    }

    /**
     * Yield detail for RAG analysis
     */
    @Data
    @Builder
    public static class YieldDetailDTO {
        private Long id;
        private LocalDate harvestDate;
        private BigDecimal yieldAmount;
        private String unit;
        private BigDecimal areaHarvested;
        private BigDecimal yieldPerUnit;
        private BigDecimal marketPrice;
        private BigDecimal totalRevenue;
        private String qualityGrade;
        private String buyer;
        private String notes;
    }
}
