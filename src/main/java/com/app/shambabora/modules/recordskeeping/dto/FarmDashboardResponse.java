package com.app.shambabora.modules.recordskeeping.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class FarmDashboardResponse {
    private String farmerName;
    private String farmName;
    private LocalDate dashboardDate;
    
    // Overview Statistics
    private Integer totalActivities;
    private Integer totalExpenses;
    private Integer totalYieldRecords;
    private Integer upcomingRemindersCount;
    
    // Financial Summary
    private BigDecimal totalExpensesAmount;
    private BigDecimal totalRevenue;
    private BigDecimal netProfit;
    private BigDecimal profitMargin;
    
    // Crop Summary
    private List<CropSummary> cropSummaries;
    private Map<String, BigDecimal> expensesByCategory;
    private Map<String, BigDecimal> expensesByGrowthStage;
    
    // Recent Activities
    private List<FarmActivityResponse> recentActivities;
    private List<FarmExpenseResponse> recentExpenses;
    private List<YieldRecordResponse> recentYields;
    
    // Upcoming Tasks
    private List<ActivityReminderResponse> upcomingReminders;
    
    // Performance Metrics
    private List<PerformanceMetric> performanceMetrics;
    
    // Recommendations
    private List<String> recommendations;
    
    @Data
    public static class CropSummary {
        private String cropType;
        private Integer activityCount;
        private BigDecimal totalExpenses;
        private BigDecimal totalYield;
        private BigDecimal totalRevenue;
        private BigDecimal averageYieldPerUnit;
        private String bestPerformingCrop;
    }
    
    @Data
    public static class PerformanceMetric {
        private String metricName;
        private String currentValue;
        private String previousValue;
        private String trend; // IMPROVING, DECLINING, STABLE
        private String unit;
    }
}
