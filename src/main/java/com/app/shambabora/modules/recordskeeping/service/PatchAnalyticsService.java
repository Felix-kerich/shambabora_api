package com.app.shambabora.modules.recordskeeping.service;

import com.app.shambabora.modules.recordskeeping.dto.PatchComparisonDTO;
import com.app.shambabora.modules.recordskeeping.dto.PatchSummaryDTO;
import com.app.shambabora.modules.recordskeeping.entity.MaizePatch;
import com.app.shambabora.modules.recordskeeping.entity.FarmActivity;
import com.app.shambabora.modules.recordskeeping.entity.FarmExpense;
import com.app.shambabora.modules.recordskeeping.entity.InputUsageRecord;
import com.app.shambabora.modules.recordskeeping.entity.YieldRecord;
import com.app.shambabora.modules.recordskeeping.repository.FarmActivityRepository;
import com.app.shambabora.modules.recordskeeping.repository.FarmExpenseRepository;
import com.app.shambabora.modules.recordskeeping.repository.InputUsageRecordRepository;
import com.app.shambabora.modules.recordskeeping.repository.MaizePatchRepository;
import com.app.shambabora.modules.recordskeeping.repository.YieldRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatchAnalyticsService {

    private final MaizePatchRepository maizePatchRepository;
    private final FarmExpenseRepository farmExpenseRepository;
    private final YieldRecordRepository yieldRecordRepository;
    private final InputUsageRecordRepository inputUsageRecordRepository;
    private final FarmActivityRepository farmActivityRepository;

    public PatchSummaryDTO getPatchSummary(Long farmerId, Long patchId) {
        MaizePatch patch = maizePatchRepository.findById(patchId)
                .filter(p -> Objects.equals(p.getFarmerProfileId(), farmerId))
                .orElseThrow(() -> new RuntimeException("Patch not found or access denied"));

        BigDecimal totalExpenses = farmExpenseRepository.getTotalExpensesByPatchId(patchId);
        if (totalExpenses == null) totalExpenses = BigDecimal.ZERO;

        BigDecimal totalYield = yieldRecordRepository.getTotalYieldByPatchId(patchId);
        if (totalYield == null) totalYield = BigDecimal.ZERO;

        BigDecimal totalRevenue = yieldRecordRepository.getTotalRevenueByPatchId(patchId);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        BigDecimal costPerKg = BigDecimal.ZERO;
        BigDecimal profit = totalRevenue.subtract(totalExpenses);
        BigDecimal profitPerKg = BigDecimal.ZERO;
        BigDecimal roi = BigDecimal.ZERO;

        if (totalYield.compareTo(BigDecimal.ZERO) > 0) {
            costPerKg = totalExpenses.divide(totalYield, 4, BigDecimal.ROUND_HALF_UP);
            profitPerKg = profit.divide(totalYield, 4, BigDecimal.ROUND_HALF_UP);
        }

        if (totalExpenses.compareTo(BigDecimal.ZERO) > 0) {
            roi = profit.divide(totalExpenses, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
        }

        List<FarmActivity> activities = farmActivityRepository.findByPatchId(patchId);
        List<InputUsageRecord> inputs = inputUsageRecordRepository.findByPatchId(patchId);
        List<FarmExpense> expenses = farmExpenseRepository.findByPatchId(patchId);

        PatchSummaryDTO dto = new PatchSummaryDTO();
        dto.setPatchId(patch.getId());
        dto.setPatchName(patch.getName());
        dto.setYear(patch.getYear());
        dto.setSeason(patch.getSeason());
        dto.setCropType(patch.getCropType());
        dto.setArea(patch.getArea());
        dto.setAreaUnit(patch.getAreaUnit());
        dto.setTotalExpenses(totalExpenses);
        dto.setTotalYield(totalYield);
        dto.setTotalRevenue(totalRevenue);
        dto.setCostPerKg(costPerKg);
        dto.setProfit(profit);
        dto.setProfitPerKg(profitPerKg);
        dto.setRoiPercentage(roi);

        dto.setActivityTypes(activities == null ? Collections.emptyList() : activities.stream()
                .map(FarmActivity::getActivityType)
                .filter(Objects::nonNull)
                .map(Enum::name)
                .distinct()
                .collect(Collectors.toList()));

        dto.setInputSummaries(inputs == null ? Collections.emptyList() : inputs.stream()
                .map(i -> {
                    String t = i.getInputType() == null ? "" : i.getInputType().name();
                    String details = "";
                    if (i.getSeedVarietyId() != null) details = "Seed:" + i.getSeedVarietyId();
                    else if (i.getFertilizerProductId() != null) details = "Fert:" + i.getFertilizerProductId();
                    else if (i.getPesticideProductId() != null) details = "Pest:" + i.getPesticideProductId();
                    return t + " " + details + " q=" + (i.getQuantityUsed() == null ? "-" : i.getQuantityUsed().toString()) + i.getUnit();
                })
                .collect(Collectors.toList()));

        dto.setExpenseSummaries(expenses == null ? Collections.emptyList() : expenses.stream()
                .map(e -> e.getCategory() + ": " + e.getAmount() + " - " + e.getDescription())
                .collect(Collectors.toList()));

        return dto;
    }

    public PatchComparisonDTO comparePatches(Long farmerId, List<Long> patchIds) {
        List<PatchSummaryDTO> summaries = patchIds.stream()
                .map(id -> getPatchSummary(farmerId, id))
                .collect(Collectors.toList());
        PatchComparisonDTO dto = new PatchComparisonDTO();
        dto.setPatches(summaries);
        return dto;
    }
}
