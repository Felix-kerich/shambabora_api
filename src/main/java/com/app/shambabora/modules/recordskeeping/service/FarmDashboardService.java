package com.app.shambabora.modules.recordskeeping.service;

import com.app.shambabora.modules.recordskeeping.dto.*;
import com.app.shambabora.modules.recordskeeping.repository.ActivityReminderRepository;
import com.app.shambabora.modules.recordskeeping.repository.FarmActivityRepository;
import com.app.shambabora.modules.recordskeeping.repository.FarmExpenseRepository;
import com.app.shambabora.modules.recordskeeping.repository.YieldRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FarmDashboardService {
    
    private final FarmActivityRepository farmActivityRepository;
    private final FarmExpenseRepository farmExpenseRepository;
    private final YieldRecordRepository yieldRecordRepository;
    private final ActivityReminderRepository activityReminderRepository;
    private final FarmExpenseService farmExpenseService;
    private final YieldRecordService yieldRecordService;

    public FarmDashboardResponse generateDashboard(Long userId) {
        FarmDashboardResponse dashboard = new FarmDashboardResponse();
        dashboard.setDashboardDate(LocalDate.now());
        
        // Overview statistics
        dashboard.setTotalActivities(farmActivityRepository.findByFarmerProfileId(userId, Pageable.unpaged()).getContent().size());
        dashboard.setTotalExpenses(farmExpenseRepository.findByFarmerProfileId(userId, Pageable.unpaged()).getContent().size());
        dashboard.setTotalYieldRecords(yieldRecordRepository.findByFarmerProfileId(userId, Pageable.unpaged()).getContent().size());
        
        // Upcoming reminders
        List<ActivityReminderResponse> upcomingReminders = new ArrayList<>();
        dashboard.setUpcomingReminders(upcomingReminders);
        dashboard.setUpcomingRemindersCount(upcomingReminders.size());
        
        // Financial summary
        BigDecimal totalExpenses = farmExpenseService.getTotalExpenses(userId, null);
        BigDecimal totalRevenue = yieldRecordService.getTotalRevenue(userId, null);
        BigDecimal netProfit = totalRevenue.subtract(totalExpenses);
        BigDecimal profitMargin = totalRevenue.compareTo(BigDecimal.ZERO) > 0 ? 
            netProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;
        
        dashboard.setTotalExpensesAmount(totalExpenses);
        dashboard.setTotalRevenue(totalRevenue);
        dashboard.setNetProfit(netProfit);
        dashboard.setProfitMargin(profitMargin);
        
        // Crop summaries
        dashboard.setCropSummaries(generateCropSummaries(userId));
        
        // Expense breakdowns
        dashboard.setExpensesByCategory(generateExpensesByCategory(userId));
        dashboard.setExpensesByGrowthStage(generateExpensesByGrowthStage(userId));
        
        // Recent activities
        dashboard.setRecentActivities(getRecentActivities(userId));
        dashboard.setRecentExpenses(getRecentExpenses(userId));
        dashboard.setRecentYields(getRecentYields(userId));
        
        // Performance metrics
        dashboard.setPerformanceMetrics(generatePerformanceMetrics(userId));
        
        // Recommendations
        dashboard.setRecommendations(generateRecommendations(dashboard));
        
        return dashboard;
    }

    private List<ActivityReminderResponse> getUpcomingReminders(Long userId) {
        return new ArrayList<>();
    }

    private List<FarmDashboardResponse.CropSummary> generateCropSummaries(Long userId) {
        List<String> cropTypes = farmActivityRepository.findByFarmerProfileId(userId, Pageable.unpaged())
                .getContent()
                .stream()
                .map(activity -> activity.getCropType())
                .distinct()
                .collect(Collectors.toList());
        
        List<FarmDashboardResponse.CropSummary> summaries = new ArrayList<>();
        for (String cropType : cropTypes) {
            FarmDashboardResponse.CropSummary summary = new FarmDashboardResponse.CropSummary();
            summary.setCropType(cropType);
            
            long activityCount = farmActivityRepository.findByFarmerProfileId(userId, Pageable.unpaged())
                    .getContent()
                    .stream()
                    .filter(activity -> activity.getCropType().equals(cropType))
                    .count();
            summary.setActivityCount((int) activityCount);
            
            BigDecimal totalExpenses = farmExpenseService.getTotalExpenses(userId, cropType);
            BigDecimal totalRevenue = yieldRecordService.getTotalRevenue(userId, cropType);
            BigDecimal totalYield = yieldRecordService.getTotalYield(userId, cropType);
            BigDecimal averageYieldPerUnit = yieldRecordService.getAverageYieldPerUnit(userId, cropType);
            
            summary.setTotalExpenses(totalExpenses);
            summary.setTotalYield(totalYield);
            summary.setTotalRevenue(totalRevenue);
            summary.setAverageYieldPerUnit(averageYieldPerUnit);
            
            summaries.add(summary);
        }
        
        if (!summaries.isEmpty()) {
            FarmDashboardResponse.CropSummary bestCrop = summaries.stream()
                    .max((s1, s2) -> {
                        BigDecimal profit1 = s1.getTotalRevenue().subtract(s1.getTotalExpenses());
                        BigDecimal profit2 = s2.getTotalRevenue().subtract(s2.getTotalExpenses());
                        return profit1.compareTo(profit2);
                    })
                    .orElse(null);
            
            if (bestCrop != null) {
                bestCrop.setBestPerformingCrop("YES");
            }
        }
        
        return summaries;
    }

    private Map<String, BigDecimal> generateExpensesByCategory(Long userId) {
        return Map.of();
    }

    private Map<String, BigDecimal> generateExpensesByGrowthStage(Long userId) {
        return Map.of();
    }

    private List<FarmActivityResponse> getRecentActivities(Long userId) {
        Pageable pageable = PageRequest.of(0, 5);
        return farmActivityRepository.findByFarmerProfileId(userId, pageable)
                .getContent()
                .stream()
                .map(activity -> {
                    FarmActivityResponse response = new FarmActivityResponse();
                    response.setId(activity.getId());
                    response.setActivityType(activity.getActivityType().name());
                    response.setCropType(activity.getCropType());
                    response.setActivityDate(activity.getActivityDate());
                    response.setDescription(activity.getDescription());
                    response.setCost(activity.getCost());
                    response.setCreatedAt(activity.getCreatedAt());
                    return response;
                })
                .collect(Collectors.toList());
    }

    private List<FarmExpenseResponse> getRecentExpenses(Long userId) {
        Pageable pageable = PageRequest.of(0, 5);
        return farmExpenseRepository.findByFarmerProfileId(userId, pageable)
                .getContent()
                .stream()
                .map(expense -> {
                    FarmExpenseResponse response = new FarmExpenseResponse();
                    response.setId(expense.getId());
                    response.setCropType(expense.getCropType());
                    response.setCategory(expense.getCategory().name());
                    response.setDescription(expense.getDescription());
                    response.setAmount(expense.getAmount());
                    response.setExpenseDate(expense.getExpenseDate());
                    response.setCreatedAt(expense.getCreatedAt());
                    return response;
                })
                .collect(Collectors.toList());
    }

    private List<YieldRecordResponse> getRecentYields(Long userId) {
        Pageable pageable = PageRequest.of(0, 5);
        return yieldRecordRepository.findByFarmerProfileId(userId, pageable)
                .getContent()
                .stream()
                .map(yield -> {
                    YieldRecordResponse response = new YieldRecordResponse();
                    response.setId(yield.getId());
                    response.setCropType(yield.getCropType());
                    response.setHarvestDate(yield.getHarvestDate());
                    response.setYieldAmount(yield.getYieldAmount());
                    response.setTotalRevenue(yield.getTotalRevenue());
                    response.setCreatedAt(yield.getCreatedAt());
                    return response;
                })
                .collect(Collectors.toList());
    }

    private List<FarmDashboardResponse.PerformanceMetric> generatePerformanceMetrics(Long userId) {
        List<FarmDashboardResponse.PerformanceMetric> metrics = new ArrayList<>();
        
        FarmDashboardResponse.PerformanceMetric yieldMetric = new FarmDashboardResponse.PerformanceMetric();
        yieldMetric.setMetricName("Average Yield");
        yieldMetric.setCurrentValue("2.5");
        yieldMetric.setPreviousValue("2.3");
        yieldMetric.setTrend("IMPROVING");
        yieldMetric.setUnit("tons/acre");
        metrics.add(yieldMetric);
        
        return metrics;
    }

    private List<String> generateRecommendations(FarmDashboardResponse dashboard) {
        List<String> recommendations = new ArrayList<>();
        
        if (dashboard.getProfitMargin().compareTo(BigDecimal.valueOf(20)) < 0) {
            recommendations.add("Consider optimizing input costs to improve profit margins");
        }
        
        if (dashboard.getUpcomingRemindersCount() > 5) {
            recommendations.add("You have many upcoming tasks. Consider prioritizing based on crop growth stage");
        }
        
        return recommendations;
    }
}
