package com.app.shambabora.modules.recordskeeping.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InputAnalyticsResponseDTO {
    
    // Overall statistics
    private String cropType;
    private Integer totalSeasonRecorded;
    private BigDecimal totalInputCost;
    private BigDecimal averageCostPerSeason;
    
    // Seed variety analysis
    private List<SeedPerformanceDTO> seedPerformance;
    private SeedPerformanceDTO bestPerformingSeed;
    private SeedPerformanceDTO mostCostEffectiveSeed;
    
    // Fertilizer analysis
    private List<FertilizerPerformanceDTO> fertilizerPerformance;
    private FertilizerPerformanceDTO bestFertilizer;
    private FertilizerPerformanceDTO mostCostEffectiveFertilizer;
    
    // Pesticide analysis
    private List<PesticidePerformanceDTO> pesticidePerformance;
    private Map<String, Double> pesticideEffectivenessRatings;
    
    // Combined input recommendations
    private List<InputCombinationDTO> recommendedInputCombinations;
    
    // ROI Analysis
    private BigDecimal averageROI;
    private BigDecimal bestSeasonROI;
    private BigDecimal worstSeasonROI;
    
    // Trends
    private List<SeasonalTrendDTO> seasonalTrends;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeedPerformanceDTO {
        private Long seedVarietyId;
        private String varietyName;
        private Integer timesUsed;
        private BigDecimal averageYield;
        private BigDecimal averageCostPerKg;
        private Integer averageEffectivenessRating;
        private BigDecimal costPerUnit;
        private String supplier;
        private Integer daysToMaturity;
        private Boolean isCurrentlyUsing;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FertilizerPerformanceDTO {
        private Long fertilizerProductId;
        private String productName;
        private String productType;
        private Integer timesUsed;
        private BigDecimal averageYieldIncrease;
        private BigDecimal costPerApplication;
        private Integer effectivenessRating;
        private Boolean isCostEffective;
        private String composition; // NPK info
        private BigDecimal ROI;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PesticidePerformanceDTO {
        private Long pesticideProductId;
        private String productName;
        private String pesticideType;
        private String targetPests;
        private Integer timesUsed;
        private Integer effectivenessRating;
        private String pestsActuallyControlled;
        private BigDecimal costPerApplication;
        private String toxicityLevel;
        private Boolean recommendedOrganic;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InputCombinationDTO {
        private String seedVariety;
        private String primaryFertilizer;
        private String primaryPesticide;
        private BigDecimal averageYield;
        private BigDecimal profitPerKg;
        private Integer timesUsedTogether;
        private Double successRate; // % of times gave good yield
        private String recommendationReason;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeasonalTrendDTO {
        private String season;
        private Integer year;
        private String seedUsed;
        private String primaryFertilizer;
        private BigDecimal totalYield;
        private BigDecimal totalInputCost;
        private BigDecimal profitPerKg;
        private BigDecimal ROI;
        private String weatherSummary;
        private String overallPerformance; // EXCELLENT, GOOD, AVERAGE, POOR
    }
}
