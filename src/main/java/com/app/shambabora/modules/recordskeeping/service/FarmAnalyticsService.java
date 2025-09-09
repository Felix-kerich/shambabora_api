package com.app.shambabora.modules.recordskeeping.service;

import com.app.shambabora.modules.recordskeeping.dto.FarmAnalyticsResponse;
import com.app.shambabora.modules.recordskeeping.entity.FarmExpense;
import com.app.shambabora.modules.recordskeeping.entity.YieldRecord;
import com.app.shambabora.modules.recordskeeping.repository.FarmExpenseRepository;
import com.app.shambabora.modules.recordskeeping.repository.YieldRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FarmAnalyticsService {
    
    private final FarmExpenseRepository farmExpenseRepository;
    private final YieldRecordRepository yieldRecordRepository;

    public FarmAnalyticsResponse generateAnalytics(Long userId, String cropType, LocalDate startDate, LocalDate endDate) {
        FarmAnalyticsResponse analytics = new FarmAnalyticsResponse();
        analytics.setCropType(cropType);
        analytics.setAnalysisPeriodStart(startDate);
        analytics.setAnalysisPeriodEnd(endDate);
        
        // Calculate financial summary
        BigDecimal totalExpenses = calculateTotalExpenses(userId, cropType, startDate, endDate);
        BigDecimal totalRevenue = calculateTotalRevenue(userId, cropType, startDate, endDate);
        BigDecimal netProfit = totalRevenue.subtract(totalExpenses);
        BigDecimal profitMargin = totalRevenue.compareTo(BigDecimal.ZERO) > 0 ? 
            netProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;
        
        analytics.setTotalExpenses(totalExpenses);
        analytics.setTotalRevenue(totalRevenue);
        analytics.setNetProfit(netProfit);
        analytics.setProfitMargin(profitMargin);
        
        // Calculate yield summary
        List<YieldRecord> yieldRecords = getYieldRecords(userId, cropType, startDate, endDate);
        if (!yieldRecords.isEmpty()) {
            BigDecimal totalYield = yieldRecords.stream()
                    .map(YieldRecord::getYieldAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal averageYieldPerUnit = yieldRecords.stream()
                    .filter(yr -> yr.getYieldPerUnit() != null)
                    .map(YieldRecord::getYieldPerUnit)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(yieldRecords.size()), 2, RoundingMode.HALF_UP);
            
            BigDecimal bestYield = yieldRecords.stream()
                    .filter(yr -> yr.getYieldPerUnit() != null)
                    .map(YieldRecord::getYieldPerUnit)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            
            BigDecimal worstYield = yieldRecords.stream()
                    .filter(yr -> yr.getYieldPerUnit() != null)
                    .map(YieldRecord::getYieldPerUnit)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            
            analytics.setTotalYield(totalYield);
            analytics.setAverageYieldPerUnit(averageYieldPerUnit);
            analytics.setBestYield(bestYield);
            analytics.setWorstYield(worstYield);
        }
        
        // Calculate expense breakdown
        analytics.setExpensesByCategory(getExpensesByCategory(userId, cropType, startDate, endDate));
        analytics.setExpensesByGrowthStage(getExpensesByGrowthStage(userId, cropType, startDate, endDate));
        
        // Generate trends
        analytics.setYieldTrends(generateYieldTrends(yieldRecords));
        analytics.setExpenseTrends(generateExpenseTrends(userId, cropType, startDate, endDate));
        analytics.setProfitabilityByPeriod(generateProfitabilityAnalysis(userId, cropType, startDate, endDate));
        
        // Generate recommendations
        analytics.setRecommendations(generateRecommendations(analytics));
        
        return analytics;
    }

    private BigDecimal calculateTotalExpenses(Long userId, String cropType, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return farmExpenseRepository.getTotalExpensesByFarmerProfileCropAndDateRange(userId, cropType, startDate, endDate);
        } else {
            return farmExpenseRepository.getTotalExpensesByFarmerProfileAndCrop(userId, cropType);
        }
    }

    private BigDecimal calculateTotalRevenue(Long userId, String cropType, LocalDate startDate, LocalDate endDate) {
        List<YieldRecord> yieldRecords = getYieldRecords(userId, cropType, startDate, endDate);
        return yieldRecords.stream()
                .filter(yr -> yr.getTotalRevenue() != null)
                .map(YieldRecord::getTotalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<YieldRecord> getYieldRecords(Long userId, String cropType, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return yieldRecordRepository.findByFarmerProfileIdAndCropTypeAndHarvestDateBetweenOrderByHarvestDateAsc(
                    userId, cropType, startDate, endDate);
        } else {
            return yieldRecordRepository.findByFarmerProfileIdAndCropTypeOrderByHarvestDateAsc(userId, cropType);
        }
    }

    private Map<String, BigDecimal> getExpensesByCategory(Long userId, String cropType, LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = farmExpenseRepository.getExpensesByCategoryForFarmerProfileAndCrop(userId, cropType);
        Map<String, BigDecimal> expensesByCategory = new HashMap<>();
        for (Object[] result : results) {
            expensesByCategory.put(result[0].toString(), (BigDecimal) result[1]);
        }
        return expensesByCategory;
    }

    private Map<String, BigDecimal> getExpensesByGrowthStage(Long userId, String cropType, LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = farmExpenseRepository.getExpensesByGrowthStageForFarmerProfileAndCrop(userId, cropType);
        Map<String, BigDecimal> expensesByGrowthStage = new HashMap<>();
        for (Object[] result : results) {
            String stage = result[0] != null ? result[0].toString() : "UNKNOWN";
            expensesByGrowthStage.put(stage, (BigDecimal) result[1]);
        }
        return expensesByGrowthStage;
    }

    private List<FarmAnalyticsResponse.YieldTrend> generateYieldTrends(List<YieldRecord> yieldRecords) {
        List<FarmAnalyticsResponse.YieldTrend> trends = new ArrayList<>();
        for (int i = 0; i < yieldRecords.size(); i++) {
            YieldRecord current = yieldRecords.get(i);
            FarmAnalyticsResponse.YieldTrend trend = new FarmAnalyticsResponse.YieldTrend();
            trend.setPeriod(current.getHarvestDate());
            trend.setYield(current.getYieldAmount());
            trend.setYieldPerUnit(current.getYieldPerUnit());
            
            if (i > 0) {
                YieldRecord previous = yieldRecords.get(i - 1);
                if (previous.getYieldPerUnit() != null && current.getYieldPerUnit() != null) {
                    int comparison = current.getYieldPerUnit().compareTo(previous.getYieldPerUnit());
                    if (comparison > 0) {
                        trend.setTrend("INCREASING");
                    } else if (comparison < 0) {
                        trend.setTrend("DECREASING");
                    } else {
                        trend.setTrend("STABLE");
                    }
                } else {
                    trend.setTrend("UNKNOWN");
                }
            } else {
                trend.setTrend("FIRST_RECORD");
            }
            
            trends.add(trend);
        }
        return trends;
    }

    private List<FarmAnalyticsResponse.ExpenseTrend> generateExpenseTrends(Long userId, String cropType, LocalDate startDate, LocalDate endDate) {
        // This is a simplified implementation. In a real scenario, you might want to group expenses by time periods
        List<FarmAnalyticsResponse.ExpenseTrend> trends = new ArrayList<>();
        // Implementation would depend on how you want to group expenses (monthly, quarterly, etc.)
        return trends;
    }

    private List<FarmAnalyticsResponse.ProfitabilityAnalysis> generateProfitabilityAnalysis(Long userId, String cropType, LocalDate startDate, LocalDate endDate) {
        List<FarmAnalyticsResponse.ProfitabilityAnalysis> analysis = new ArrayList<>();
        // This would analyze profitability over different time periods
        // Implementation depends on your specific requirements
        return analysis;
    }

    private List<String> generateRecommendations(FarmAnalyticsResponse analytics) {
        List<String> recommendations = new ArrayList<>();
        
        // Profitability recommendations
        if (analytics.getProfitMargin().compareTo(BigDecimal.valueOf(20)) < 0) {
            recommendations.add("Consider optimizing input costs to improve profit margins");
        }
        
        // Yield recommendations
        if (analytics.getBestYield() != null && analytics.getAverageYieldPerUnit() != null) {
            BigDecimal yieldGap = analytics.getBestYield().subtract(analytics.getAverageYieldPerUnit());
            if (yieldGap.compareTo(analytics.getAverageYieldPerUnit().multiply(BigDecimal.valueOf(0.2))) > 0) {
                recommendations.add("There's significant potential to improve yield. Consider analyzing best practices from your highest-yielding harvests");
            }
        }
        
        // Expense recommendations
        Map<String, BigDecimal> expensesByCategory = analytics.getExpensesByCategory();
        if (expensesByCategory.containsKey("FERTILIZER") && expensesByCategory.containsKey("PESTICIDES")) {
            BigDecimal fertilizerCost = expensesByCategory.get("FERTILIZER");
            BigDecimal pesticideCost = expensesByCategory.get("PESTICIDES");
            if (pesticideCost.compareTo(fertilizerCost.multiply(BigDecimal.valueOf(0.5))) > 0) {
                recommendations.add("Pesticide costs are high relative to fertilizer costs. Consider integrated pest management strategies");
            }
        }
        
        return recommendations;
    }
}
