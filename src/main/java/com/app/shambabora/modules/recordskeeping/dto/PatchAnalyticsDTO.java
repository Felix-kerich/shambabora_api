package com.app.shambabora.modules.recordskeeping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Detailed analytics for a single patch/plot
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatchAnalyticsDTO {
    private Long patchId;
    private String patchName;
    private String season;
    private Integer year;
    private String location;
    private Double area;
    private String areaUnit;
    private LocalDate plantingDate;
    private LocalDate actualHarvestDate;
    
    // Financial Metrics
    private BigDecimal totalExpenses;
    private BigDecimal totalRevenue;
    private BigDecimal netProfit;
    private BigDecimal profitMargin; // percentage
    
    // Yield Metrics
    private BigDecimal totalYield;
    private BigDecimal yieldPerUnit;
    private BigDecimal yieldPerHectare; // or per acre depending on unit
    
    // Resource Consumption
    private BigDecimal labourCost;
    private BigDecimal fertiliserCost;
    private BigDecimal pesticideCost;
    private BigDecimal seedsCost;
    private BigDecimal otherCosts;
    
    // Expense Breakdown
    private Map<String, BigDecimal> expensesByCategory;
    private Map<String, BigDecimal> expensesByGrowthStage;
    
    // Performance Indicators
    private String performanceRating; // EXCELLENT, GOOD, AVERAGE, POOR
    private BigDecimal costPerUnitProduced;
    private BigDecimal revenuePerCostRatio; // How much revenue per 1 unit of cost spent
}
