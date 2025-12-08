package com.app.shambabora.modules.recordskeeping.service;

import com.app.shambabora.modules.recordskeeping.dto.PatchAnalyticsDTO;
import com.app.shambabora.modules.recordskeeping.dto.PatchesAnalyticsSummaryDTO;
import com.app.shambabora.modules.recordskeeping.entity.FarmExpense;
import com.app.shambabora.modules.recordskeeping.entity.MaizePatch;
import com.app.shambabora.modules.recordskeeping.entity.YieldRecord;
import com.app.shambabora.modules.recordskeeping.repository.FarmExpenseRepository;
import com.app.shambabora.modules.recordskeeping.repository.MaizePatchRepository;
import com.app.shambabora.modules.recordskeeping.repository.YieldRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatchComprehensiveAnalyticsService {

    private final MaizePatchRepository maizePatchRepository;
    private final FarmExpenseRepository farmExpenseRepository;
    private final YieldRecordRepository yieldRecordRepository;

    /**
     * Generate comprehensive analytics for all patches of a farmer
     */
    public PatchesAnalyticsSummaryDTO generatePatchesAnalytics(Long userId) {
        log.info("Generating patches analytics for user: {}", userId);
        
        // Get all patches for the farmer
        List<MaizePatch> patches = maizePatchRepository.findByFarmerProfileIdOrderByYearDesc(userId);
        
        if (patches.isEmpty()) {
            log.warn("No patches found for user: {}", userId);
            return PatchesAnalyticsSummaryDTO.builder()
                    .totalPatches(0)
                    .patchesAnalyzed(0)
                    .allPatchesAnalytics(new ArrayList<>())
                    .build();
        }
        
        // Generate analytics for each patch
        List<PatchAnalyticsDTO> allPatchesAnalytics = patches.stream()
                .map(this::generatePatchAnalytics)
                .collect(Collectors.toList());
        
        // Use all patches for analysis (don't filter out empty ones)
        // This allows us to identify worst performing patches even if they have no data
        List<PatchAnalyticsDTO> analyzedPatches = new ArrayList<>(allPatchesAnalytics);
        
        // Build summary
        PatchesAnalyticsSummaryDTO summary = PatchesAnalyticsSummaryDTO.builder()
                .totalPatches(patches.size())
                .patchesAnalyzed(analyzedPatches.size())
                .bestPerformingPatch(findBestPerformingPatch(analyzedPatches))
                .worstPerformingPatch(findWorstPerformingPatch(analyzedPatches))
                .mostResourceIntensivePatch(findMostResourceIntensivePatch(analyzedPatches))
                .highestLabourCostPatch(findHighestLabourCostPatch(analyzedPatches))
                .mostProfitablePatch(findMostProfitablePatch(analyzedPatches))
                .highestExpensesPatch(findHighestExpensesPatch(analyzedPatches))
                .averageMetrics(calculateAverageMetrics(analyzedPatches))
                .allPatchesAnalytics(allPatchesAnalytics)
                .build();
        
        log.info("Patches analytics generated successfully for user: {}", userId);
        return summary;
    }

    /**
     * Generate analytics for a single patch
     */
    private PatchAnalyticsDTO generatePatchAnalytics(MaizePatch patch) {
        log.debug("Generating analytics for patch: {} ({})", patch.getId(), patch.getName());
        
        // Get yield data
        List<YieldRecord> yieldRecords = yieldRecordRepository.findByPatchId(patch.getId());
        BigDecimal totalYield = BigDecimal.ZERO;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal yieldPerUnit = BigDecimal.ZERO;
        
        if (!yieldRecords.isEmpty()) {
            totalYield = yieldRecords.stream()
                    .map(YieldRecord::getYieldAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            totalRevenue = yieldRecords.stream()
                    .map(YieldRecord::getTotalRevenue)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            yieldPerUnit = yieldRecords.stream()
                    .map(YieldRecord::getYieldPerUnit)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(yieldRecords.size()), 2, RoundingMode.HALF_UP);
        }
        
        // Get expense data
        BigDecimal totalExpenses = getOrZero(farmExpenseRepository.getTotalExpensesByPatchId(patch.getId()));
        BigDecimal labourCost = getOrZero(farmExpenseRepository.getTotalLabourCostByPatchId(patch.getId()));
        BigDecimal fertiliserCost = getOrZero(farmExpenseRepository.getTotalFertiliserCostByPatchId(patch.getId()));
        BigDecimal pesticideCost = getOrZero(farmExpenseRepository.getTotalPesticideCostByPatchId(patch.getId()));
        BigDecimal seedsCost = getOrZero(farmExpenseRepository.getTotalSeedCostByPatchId(patch.getId()));
        
        // Calculate derived metrics
        BigDecimal netProfit = totalRevenue.subtract(totalExpenses);
        BigDecimal profitMargin = totalRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                netProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) :
                BigDecimal.ZERO;
        
        BigDecimal yieldPerHectare = BigDecimal.ZERO;
        if (patch.getArea() != null && patch.getArea() > 0) {
            yieldPerHectare = totalYield.divide(BigDecimal.valueOf(patch.getArea()), 2, RoundingMode.HALF_UP);
        }
        
        BigDecimal costPerUnitProduced = BigDecimal.ZERO;
        if (totalYield.compareTo(BigDecimal.ZERO) > 0) {
            costPerUnitProduced = totalExpenses.divide(totalYield, 4, RoundingMode.HALF_UP);
        }
        
        BigDecimal revenuePerCostRatio = BigDecimal.ZERO;
        if (totalExpenses.compareTo(BigDecimal.ZERO) > 0) {
            revenuePerCostRatio = totalRevenue.divide(totalExpenses, 4, RoundingMode.HALF_UP);
        }
        
        // Determine performance rating
        String performanceRating = determinePerformanceRating(profitMargin, yieldPerHectare);
        
        // Get expense breakdown
        Map<String, BigDecimal> expensesByCategory = getExpensesByCategoryMap(patch.getId());
        Map<String, BigDecimal> expensesByGrowthStage = getExpensesByGrowthStageMap(patch.getId());
        
        return PatchAnalyticsDTO.builder()
                .patchId(patch.getId())
                .patchName(patch.getName())
                .season(patch.getSeason())
                .year(patch.getYear())
                .location(patch.getLocation())
                .area(patch.getArea())
                .areaUnit(patch.getAreaUnit())
                .plantingDate(patch.getPlantingDate())
                .actualHarvestDate(patch.getActualHarvestDate())
                
                // Financial metrics
                .totalExpenses(totalExpenses)
                .totalRevenue(totalRevenue)
                .netProfit(netProfit)
                .profitMargin(profitMargin)
                
                // Yield metrics
                .totalYield(totalYield)
                .yieldPerUnit(yieldPerUnit)
                .yieldPerHectare(yieldPerHectare)
                
                // Resource consumption
                .labourCost(labourCost)
                .fertiliserCost(fertiliserCost)
                .pesticideCost(pesticideCost)
                .seedsCost(seedsCost)
                .otherCosts(totalExpenses.subtract(labourCost).subtract(fertiliserCost).subtract(pesticideCost).subtract(seedsCost))
                
                // Expense breakdown
                .expensesByCategory(expensesByCategory)
                .expensesByGrowthStage(expensesByGrowthStage)
                
                // Performance indicators
                .performanceRating(performanceRating)
                .costPerUnitProduced(costPerUnitProduced)
                .revenuePerCostRatio(revenuePerCostRatio)
                .build();
    }

    /**
     * Find the best performing patch based on profit margin and yield per hectare
     */
    private PatchAnalyticsDTO findBestPerformingPatch(List<PatchAnalyticsDTO> patches) {
        if (patches.isEmpty()) return null;
        
        return patches.stream()
                .max(Comparator
                        .comparing((PatchAnalyticsDTO p) -> p.getProfitMargin() != null ? p.getProfitMargin() : BigDecimal.ZERO)
                        .thenComparing((PatchAnalyticsDTO p) -> p.getYieldPerHectare() != null ? p.getYieldPerHectare() : BigDecimal.ZERO)
                        .thenComparing((PatchAnalyticsDTO p) -> p.getNetProfit() != null ? p.getNetProfit() : BigDecimal.ZERO))
                .orElse(null);
    }

    /**
     * Find the worst performing patch - lowest profit margin and yield
     */
    private PatchAnalyticsDTO findWorstPerformingPatch(List<PatchAnalyticsDTO> patches) {
        if (patches.isEmpty()) return null;
        
        return patches.stream()
                .min(Comparator
                        .comparing((PatchAnalyticsDTO p) -> p.getProfitMargin() != null ? p.getProfitMargin() : BigDecimal.ZERO)
                        .thenComparing((PatchAnalyticsDTO p) -> p.getYieldPerHectare() != null ? p.getYieldPerHectare() : BigDecimal.ZERO)
                        .thenComparing((PatchAnalyticsDTO p) -> p.getNetProfit() != null ? p.getNetProfit() : BigDecimal.ZERO))
                .orElse(null);
    }

    /**
     * Find the patch that consumes the most resources (highest total expenses)
     */
    private PatchAnalyticsDTO findMostResourceIntensivePatch(List<PatchAnalyticsDTO> patches) {
        if (patches.isEmpty()) return null;
        
        return patches.stream()
                .max(Comparator.comparing(p -> p.getTotalExpenses() != null ? p.getTotalExpenses() : BigDecimal.ZERO))
                .orElse(null);
    }

    /**
     * Find the patch with the highest labour cost
     */
    private PatchAnalyticsDTO findHighestLabourCostPatch(List<PatchAnalyticsDTO> patches) {
        if (patches.isEmpty()) return null;
        
        return patches.stream()
                .max(Comparator.comparing(p -> p.getLabourCost() != null ? p.getLabourCost() : BigDecimal.ZERO))
                .orElse(null);
    }

    /**
     * Find the most profitable patch
     */
    private PatchAnalyticsDTO findMostProfitablePatch(List<PatchAnalyticsDTO> patches) {
        if (patches.isEmpty()) return null;
        
        return patches.stream()
                .max(Comparator.comparing(p -> p.getNetProfit() != null ? p.getNetProfit() : BigDecimal.ZERO))
                .orElse(null);
    }

    /**
     * Find the patch with the highest total expenses
     */
    private PatchAnalyticsDTO findHighestExpensesPatch(List<PatchAnalyticsDTO> patches) {
        if (patches.isEmpty()) return null;
        
        return patches.stream()
                .max(Comparator.comparing(p -> p.getTotalExpenses() != null ? p.getTotalExpenses() : BigDecimal.ZERO))
                .orElse(null);
    }

    /**
     * Calculate average metrics across all patches
     */
    private PatchAnalyticsDTO calculateAverageMetrics(List<PatchAnalyticsDTO> patches) {
        if (patches.isEmpty()) return null;
        
        int size = patches.size();
        
        BigDecimal avgExpenses = patches.stream()
                .map(PatchAnalyticsDTO::getTotalExpenses)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(size), 2, RoundingMode.HALF_UP);
        
        BigDecimal avgRevenue = patches.stream()
                .map(PatchAnalyticsDTO::getTotalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(size), 2, RoundingMode.HALF_UP);
        
        BigDecimal avgProfit = patches.stream()
                .map(PatchAnalyticsDTO::getNetProfit)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(size), 2, RoundingMode.HALF_UP);
        
        BigDecimal avgProfitMargin = patches.stream()
                .map(PatchAnalyticsDTO::getProfitMargin)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(size), 2, RoundingMode.HALF_UP);
        
        BigDecimal avgYield = patches.stream()
                .map(PatchAnalyticsDTO::getTotalYield)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(size), 2, RoundingMode.HALF_UP);
        
        BigDecimal avgYieldPerHectare = patches.stream()
                .map(PatchAnalyticsDTO::getYieldPerHectare)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(size), 2, RoundingMode.HALF_UP);
        
        BigDecimal avgLabourCost = patches.stream()
                .map(PatchAnalyticsDTO::getLabourCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(size), 2, RoundingMode.HALF_UP);
        
        return PatchAnalyticsDTO.builder()
                .patchName("AVERAGE")
                .totalExpenses(avgExpenses)
                .totalRevenue(avgRevenue)
                .netProfit(avgProfit)
                .profitMargin(avgProfitMargin)
                .totalYield(avgYield)
                .yieldPerHectare(avgYieldPerHectare)
                .labourCost(avgLabourCost)
                .performanceRating("AVERAGE")
                .build();
    }

    /**
     * Determine performance rating based on profitability and yield
     */
    private String determinePerformanceRating(BigDecimal profitMargin, BigDecimal yieldPerHectare) {
        // High profit margin (>30%) and good yield = EXCELLENT
        if (profitMargin.compareTo(BigDecimal.valueOf(30)) > 0 && yieldPerHectare.compareTo(BigDecimal.valueOf(5)) > 0) {
            return "EXCELLENT";
        }
        // Good profit margin (15-30%) = GOOD
        if (profitMargin.compareTo(BigDecimal.valueOf(15)) > 0) {
            return "GOOD";
        }
        // Average profit margin (5-15%) = AVERAGE
        if (profitMargin.compareTo(BigDecimal.valueOf(5)) > 0) {
            return "AVERAGE";
        }
        // Low or negative profit margin = POOR
        return "POOR";
    }

    /**
     * Get expenses by category for a patch as a map
     */
    private Map<String, BigDecimal> getExpensesByCategoryMap(Long patchId) {
        List<Object[]> results = farmExpenseRepository.getExpensesByCategoryByPatchId(patchId);
        Map<String, BigDecimal> map = new HashMap<>();
        for (Object[] result : results) {
            if (result[0] != null) {
                FarmExpense.ExpenseCategory category = (FarmExpense.ExpenseCategory) result[0];
                BigDecimal amount = result[1] != null ? (BigDecimal) result[1] : BigDecimal.ZERO;
                map.put(category.toString(), amount);
            }
        }
        return map;
    }

    /**
     * Get expenses by growth stage for a patch as a map
     */
    private Map<String, BigDecimal> getExpensesByGrowthStageMap(Long patchId) {
        List<Object[]> results = farmExpenseRepository.getExpensesByGrowthStageByPatchId(patchId);
        Map<String, BigDecimal> map = new HashMap<>();
        for (Object[] result : results) {
            String stage = result[0] != null ? result[0].toString() : "UNKNOWN";
            BigDecimal amount = result[1] != null ? (BigDecimal) result[1] : BigDecimal.ZERO;
            map.put(stage, amount);
        }
        return map;
    }

    /**
     * Helper method to convert null to zero
     */
    private BigDecimal getOrZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
