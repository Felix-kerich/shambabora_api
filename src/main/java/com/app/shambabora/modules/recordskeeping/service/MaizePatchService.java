package com.app.shambabora.modules.recordskeeping.service;

import com.app.shambabora.modules.recordskeeping.dto.*;
import com.app.shambabora.modules.recordskeeping.entity.FarmActivity;
import com.app.shambabora.modules.recordskeeping.entity.FarmExpense;
import com.app.shambabora.modules.recordskeeping.entity.MaizePatch;
import com.app.shambabora.modules.recordskeeping.entity.YieldRecord;
import com.app.shambabora.modules.recordskeeping.repository.FarmActivityRepository;
import com.app.shambabora.modules.recordskeeping.repository.FarmExpenseRepository;
import com.app.shambabora.modules.recordskeeping.repository.MaizePatchRepository;
import com.app.shambabora.modules.recordskeeping.repository.YieldRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaizePatchService {

    private final MaizePatchRepository maizePatchRepository;
    private final FarmActivityRepository farmActivityRepository;
    private final YieldRecordRepository yieldRecordRepository;
    private final FarmExpenseRepository farmExpenseRepository;

    public MaizePatchDTO createPatch(MaizePatchDTO dto) {
        MaizePatch p = mapFromDTO(dto);
        MaizePatch saved = maizePatchRepository.save(p);
        return mapToDTO(saved);
    }

    public List<MaizePatchDTO> listPatches(Long farmerProfileId) {
        List<MaizePatch> patches = maizePatchRepository.findByFarmerProfileIdOrderByYearDesc(farmerProfileId);
        return patches.stream().map(patch -> {
            MaizePatchDTO dto = mapToDTO(patch);
            
            // Fetch and attach related records for each patch
            List<FarmActivity> activities = farmActivityRepository.findByPatchId(patch.getId());
            List<YieldRecord> yields = yieldRecordRepository.findByPatchId(patch.getId());
            List<FarmExpense> expenses = farmExpenseRepository.findByPatchId(patch.getId());
            
            dto.setActivities(activities.stream().map(this::mapActivity).collect(Collectors.toList()));
            dto.setYields(yields.stream().map(this::mapYield).collect(Collectors.toList()));
            dto.setExpenses(expenses.stream().map(this::mapExpense).collect(Collectors.toList()));
            
            return dto;
        }).collect(Collectors.toList());
    }

    public MaizePatchDTO getPatch(Long farmerProfileId, Long patchId) {
        MaizePatch p = maizePatchRepository.findById(patchId)
                .filter(x -> x.getFarmerProfileId().equals(farmerProfileId))
                .orElseThrow(() -> new RuntimeException("Patch not found or access denied"));

        MaizePatchDTO dto = mapToDTO(p);

        // Fetch related records by patch id and attach to DTO
        List<FarmActivity> activities = farmActivityRepository.findByPatchId(patchId);
        List<YieldRecord> yields = yieldRecordRepository.findByPatchId(patchId);
        List<FarmExpense> expenses = farmExpenseRepository.findByPatchId(patchId);

        dto.setActivities(activities.stream().map(this::mapActivity).collect(Collectors.toList()));
        dto.setYields(yields.stream().map(this::mapYield).collect(Collectors.toList()));
        dto.setExpenses(expenses.stream().map(this::mapExpense).collect(Collectors.toList()));

        return dto;
    }

    /**
     * Get all patches with their related records in RAG-optimized format for AI analysis.
     * This comprehensive data is sent to the RAG service for personalized farmer recommendations.
     */
    public List<PatchRagDataDTO> getAllPatchesForRag(Long farmerProfileId) {
        List<MaizePatch> patches = maizePatchRepository.findByFarmerProfileIdOrderByYearDesc(farmerProfileId);
        return patches.stream()
                .map(patch -> buildRagDataForPatch(patch))
                .collect(Collectors.toList());
    }

    /**
     * Build comprehensive RAG data for a single patch including metrics and analytics.
     */
    public PatchRagDataDTO buildRagDataForPatch(MaizePatch patch) {
        List<FarmActivity> activities = farmActivityRepository.findByPatchId(patch.getId());
        List<YieldRecord> yields = yieldRecordRepository.findByPatchId(patch.getId());
        List<FarmExpense> expenses = farmExpenseRepository.findByPatchId(patch.getId());

        // Compute aggregates
        BigDecimal totalExpenses = expenses.isEmpty() ? BigDecimal.ZERO
                : expenses.stream()
                .map(FarmExpense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalYield = yields.isEmpty() ? BigDecimal.ZERO
                : yields.stream()
                .map(YieldRecord::getYieldAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRevenue = yields.isEmpty() ? BigDecimal.ZERO
                : yields.stream()
                .map(yr -> {
                    BigDecimal revenue = yr.getYieldAmount() != null && yr.getMarketPrice() != null
                            ? yr.getYieldAmount().multiply(yr.getMarketPrice())
                            : BigDecimal.ZERO;
                    return revenue;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal costPerKg = BigDecimal.ZERO;
        BigDecimal profit = totalRevenue.subtract(totalExpenses);
        BigDecimal profitPerKg = BigDecimal.ZERO;
        BigDecimal roiPercentage = BigDecimal.ZERO;

        if (totalYield.compareTo(BigDecimal.ZERO) > 0) {
            costPerKg = totalExpenses.divide(totalYield, 4, BigDecimal.ROUND_HALF_UP);
            profitPerKg = profit.divide(totalYield, 4, BigDecimal.ROUND_HALF_UP);
        }

        if (totalExpenses.compareTo(BigDecimal.ZERO) > 0) {
            roiPercentage = profit.divide(totalExpenses, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
        }

        // Build DTO
        return PatchRagDataDTO.builder()
                .patchId(patch.getId())
                .farmerProfileId(patch.getFarmerProfileId())
                .year(patch.getYear())
                .season(patch.getSeason())
                .patchName(patch.getName())
                .cropType(patch.getCropType())
                .area(patch.getArea())
                .areaUnit(patch.getAreaUnit())
                .plantingDate(patch.getPlantingDate())
                .expectedHarvestDate(patch.getExpectedHarvestDate())
                .actualHarvestDate(patch.getActualHarvestDate())
                .location(patch.getLocation())
                .notes(patch.getNotes())
                .totalExpenses(totalExpenses)
                .totalYield(totalYield)
                .totalRevenue(totalRevenue)
                .costPerKg(costPerKg)
                .profit(profit)
                .profitPerKg(profitPerKg)
                .roiPercentage(roiPercentage)
                .activities(activities.stream().map(this::mapActivityForRag).collect(Collectors.toList()))
                .expenses(expenses.stream().map(this::mapExpenseForRag).collect(Collectors.toList()))
                .yields(yields.stream().map(this::mapYieldForRag).collect(Collectors.toList()))
                .build();
    }

    private PatchRagDataDTO.ActivityDetailDTO mapActivityForRag(FarmActivity a) {
        return PatchRagDataDTO.ActivityDetailDTO.builder()
                .id(a.getId())
                .activityType(a.getActivityType() != null ? a.getActivityType().name() : null)
                .activityDate(a.getActivityDate())
                .description(a.getDescription())
                .areaSize(a.getAreaSize())
                .units(a.getUnits())
                .productUsed(a.getProductUsed())
                .applicationRate(a.getApplicationRate())
                .weatherConditions(a.getWeatherConditions())
                .soilConditions(a.getSoilConditions())
                .laborHours(a.getLaborHours())
                .equipmentUsed(a.getEquipmentUsed())
                .laborCost(a.getLaborCost())
                .equipmentCost(a.getEquipmentCost())
                .notes(a.getNotes())
                .build();
    }

    private PatchRagDataDTO.ExpenseDetailDTO mapExpenseForRag(FarmExpense e) {
        return PatchRagDataDTO.ExpenseDetailDTO.builder()
                .id(e.getId())
                .category(e.getCategory() != null ? e.getCategory().name() : null)
                .description(e.getDescription())
                .amount(e.getAmount())
                .expenseDate(e.getExpenseDate())
                .supplier(e.getSupplier())
                .growthStage(e.getGrowthStage() != null ? e.getGrowthStage().name() : null)
                .notes(e.getNotes())
                .build();
    }

    private PatchRagDataDTO.YieldDetailDTO mapYieldForRag(YieldRecord y) {
        return PatchRagDataDTO.YieldDetailDTO.builder()
                .id(y.getId())
                .harvestDate(y.getHarvestDate())
                .yieldAmount(y.getYieldAmount())
                .unit(y.getUnit())
                .areaHarvested(y.getAreaHarvested())
                .yieldPerUnit(y.getYieldPerUnit())
                .marketPrice(y.getMarketPrice())
                .totalRevenue(y.getTotalRevenue())
                .qualityGrade(y.getQualityGrade())
                .buyer(y.getBuyer())
                .notes(y.getNotes())
                .build();
    }

    private MaizePatchDTO mapToDTO(MaizePatch p) {
        MaizePatchDTO d = new MaizePatchDTO();
        d.setId(p.getId());
        d.setFarmerProfileId(p.getFarmerProfileId());
        d.setYear(p.getYear());
        d.setSeason(p.getSeason());
        d.setName(p.getName());
        d.setCropType(p.getCropType());
        d.setArea(p.getArea());
        d.setAreaUnit(p.getAreaUnit());
        d.setPlantingDate(p.getPlantingDate());
        d.setExpectedHarvestDate(p.getExpectedHarvestDate());
        d.setActualHarvestDate(p.getActualHarvestDate());
        d.setLocation(p.getLocation());
        d.setNotes(p.getNotes());
        d.setCreatedAt(p.getCreatedAt());
        d.setUpdatedAt(p.getUpdatedAt());
        return d;
    }

    private MaizePatch mapFromDTO(MaizePatchDTO d) {
        MaizePatch p = new MaizePatch();
        // Only set id when provided and valid (>0). If id is null or 0, leave it null so JPA will treat
        // this as a new entity and avoid merge/optimistic locking issues caused by id==0.
        if (d.getId() != null && d.getId() > 0) {
            p.setId(d.getId());
        }
        p.setFarmerProfileId(d.getFarmerProfileId());
        p.setYear(d.getYear());
        p.setSeason(d.getSeason());
        p.setName(d.getName());
        p.setCropType(d.getCropType());
        p.setArea(d.getArea());
        p.setAreaUnit(d.getAreaUnit());
        p.setPlantingDate(d.getPlantingDate());
        p.setExpectedHarvestDate(d.getExpectedHarvestDate());
        p.setActualHarvestDate(d.getActualHarvestDate());
        p.setLocation(d.getLocation());
        p.setNotes(d.getNotes());
        return p;
    }

    private FarmActivityResponse mapActivity(FarmActivity a) {
        FarmActivityResponse r = new FarmActivityResponse();
        r.setId(a.getId());
        r.setPatchId(a.getPatchId());
        r.setPatchName(a.getPatchName());
        r.setActivityType(a.getActivityType() != null ? a.getActivityType().name() : null);
        r.setCropType(a.getCropType());
        r.setActivityDate(a.getActivityDate());
        r.setDescription(a.getDescription());
        r.setAreaSize(a.getAreaSize());
        r.setUnits(a.getUnits());
        r.setYield(a.getYield());
        r.setCost(a.getCost());
        r.setProductUsed(a.getProductUsed());
        r.setApplicationRate(a.getApplicationRate());
        r.setWeatherConditions(a.getWeatherConditions());
        r.setSoilConditions(a.getSoilConditions());
        r.setNotes(a.getNotes());
        r.setLocation(a.getLocation());
        r.setLaborHours(a.getLaborHours());
        r.setEquipmentUsed(a.getEquipmentUsed());
        r.setLaborCost(a.getLaborCost());
        r.setEquipmentCost(a.getEquipmentCost());
        r.setCreatedAt(a.getCreatedAt());
        r.setUpdatedAt(a.getUpdatedAt());
        return r;
    }

    private YieldRecordResponse mapYield(YieldRecord y) {
        YieldRecordResponse r = new YieldRecordResponse();
        r.setId(y.getId());
        r.setPatchId(y.getPatchId());
        r.setPatchName(y.getPatchName());
        r.setCropType(y.getCropType());
        r.setHarvestDate(y.getHarvestDate());
        r.setYieldAmount(y.getYieldAmount());
        r.setUnit(y.getUnit());
        r.setAreaHarvested(y.getAreaHarvested());
        r.setYieldPerUnit(y.getYieldPerUnit());
        r.setMarketPrice(y.getMarketPrice());
        r.setTotalRevenue(y.getTotalRevenue());
        r.setQualityGrade(y.getQualityGrade());
        r.setStorageLocation(y.getStorageLocation());
        r.setBuyer(y.getBuyer());
        r.setNotes(y.getNotes());
        r.setFarmActivityId(y.getFarmActivityId());
        r.setCreatedAt(y.getCreatedAt());
        r.setUpdatedAt(y.getUpdatedAt());
        return r;
    }

    private FarmExpenseResponse mapExpense(FarmExpense e) {
        FarmExpenseResponse r = new FarmExpenseResponse();
        r.setId(e.getId());
        r.setPatchId(e.getPatchId());
        r.setPatchName(e.getPatchName());
        r.setCropType(e.getCropType());
        r.setCategory(e.getCategory() != null ? e.getCategory().name() : null);
        r.setDescription(e.getDescription());
        r.setAmount(e.getAmount());
        r.setExpenseDate(e.getExpenseDate());
        r.setSupplier(e.getSupplier());
        r.setInvoiceNumber(e.getInvoiceNumber());
        r.setPaymentMethod(e.getPaymentMethod());
        r.setNotes(e.getNotes());
        r.setGrowthStage(e.getGrowthStage() != null ? e.getGrowthStage().name() : null);
        r.setFarmActivityId(e.getFarmActivityId());
        r.setIsRecurring(e.getIsRecurring());
        r.setRecurringFrequency(e.getRecurringFrequency());
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        return r;
    }
}
