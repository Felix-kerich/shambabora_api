package com.app.shambabora.modules.recordskeeping.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class FarmAnalyticsResponse {
    private String cropType;
    private LocalDate analysisPeriodStart;
    private LocalDate analysisPeriodEnd;
    
    // Financial Summary
    private BigDecimal totalExpenses;
    private BigDecimal totalRevenue;
    private BigDecimal netProfit;
    private BigDecimal profitMargin;
    
    // Yield Summary
    private BigDecimal totalYield;
    private BigDecimal averageYieldPerUnit;
    private BigDecimal bestYield;
    private BigDecimal worstYield;
    
    // Expense Breakdown
    private Map<String, BigDecimal> expensesByCategory;
    private Map<String, BigDecimal> expensesByGrowthStage;
    
    // Performance Metrics
    private List<YieldTrend> yieldTrends;
    private List<ExpenseTrend> expenseTrends;
    private List<ProfitabilityAnalysis> profitabilityByPeriod;
    
    // Recommendations
    private List<String> recommendations;
    
    @Data
    public static class YieldTrend {
        private LocalDate period;
        private BigDecimal yield;
        private BigDecimal yieldPerUnit;
        private String trend; // INCREASING, DECREASING, STABLE
    }
    
    @Data
    public static class ExpenseTrend {
        private LocalDate period;
        private String category;
        private BigDecimal amount;
        private String trend; // INCREASING, DECREASING, STABLE
    }
    
    @Data
    public static class ProfitabilityAnalysis {
        private LocalDate period;
        private BigDecimal revenue;
        private BigDecimal expenses;
        private BigDecimal profit;
        private BigDecimal profitMargin;
    }
}
