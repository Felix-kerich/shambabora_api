package com.app.shambabora.modules.recordskeeping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Summary of all patches with performance rankings
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatchesAnalyticsSummaryDTO {
    
    private Integer totalPatches;
    private Integer patchesAnalyzed;
    
    // Performance Rankings
    private PatchAnalyticsDTO bestPerformingPatch;
    private PatchAnalyticsDTO worstPerformingPatch;
    private PatchAnalyticsDTO mostResourceIntensivePatch;
    private PatchAnalyticsDTO highestLabourCostPatch;
    private PatchAnalyticsDTO mostProfitablePatch;
    private PatchAnalyticsDTO highestExpensesPatch;
    
    // Overall Statistics
    private PatchAnalyticsDTO averageMetrics;
    
    // Detailed List
    private List<PatchAnalyticsDTO> allPatchesAnalytics;
}
