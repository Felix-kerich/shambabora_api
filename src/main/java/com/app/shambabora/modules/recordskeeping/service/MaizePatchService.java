package com.app.shambabora.modules.recordskeeping.service;

import com.app.shambabora.modules.recordskeeping.dto.FarmActivityResponse;
import com.app.shambabora.modules.recordskeeping.dto.FarmExpenseResponse;
import com.app.shambabora.modules.recordskeeping.dto.MaizePatchDTO;
import com.app.shambabora.modules.recordskeeping.dto.YieldRecordResponse;
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

import java.util.List;
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
