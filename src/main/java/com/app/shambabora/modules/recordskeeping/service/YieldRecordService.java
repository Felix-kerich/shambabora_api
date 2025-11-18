package com.app.shambabora.modules.recordskeeping.service;

import com.app.shambabora.modules.recordskeeping.dto.YieldRecordRequest;
import com.app.shambabora.modules.recordskeeping.dto.YieldRecordResponse;
import com.app.shambabora.modules.recordskeeping.entity.YieldRecord;
import com.app.shambabora.modules.recordskeeping.repository.FarmActivityRepository;
import com.app.shambabora.modules.recordskeeping.repository.MaizePatchRepository;
import com.app.shambabora.modules.recordskeeping.entity.MaizePatch;
import com.app.shambabora.modules.recordskeeping.repository.YieldRecordRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class YieldRecordService {
    
    private final YieldRecordRepository yieldRecordRepository;
    private final FarmActivityRepository farmActivityRepository;
    private final MaizePatchRepository maizePatchRepository;

    @Transactional
    public YieldRecordResponse createYieldRecord(Long userId, YieldRecordRequest request) {
    Long farmActivityId = request.getFarmActivityId();

    MaizePatch patch = maizePatchRepository.findById(request.getPatchId())
        .filter(p -> p.getFarmerProfileId().equals(userId))
        .orElseThrow(() -> new EntityNotFoundException("Patch not found or access denied"));
        
        BigDecimal yieldPerUnit = null;
        if (request.getAreaHarvested() != null && request.getAreaHarvested().compareTo(BigDecimal.ZERO) > 0) {
            yieldPerUnit = request.getYieldAmount().divide(request.getAreaHarvested(), 2, BigDecimal.ROUND_HALF_UP);
        }
        
        BigDecimal totalRevenue = null;
        if (request.getMarketPrice() != null) {
            totalRevenue = request.getYieldAmount().multiply(request.getMarketPrice());
        }
        
        YieldRecord yieldRecord = YieldRecord.builder()
                .farmerProfileId(userId)
                .cropType(request.getCropType())
                .harvestDate(request.getHarvestDate())
                .yieldAmount(request.getYieldAmount())
                .unit(request.getUnit())
                .areaHarvested(request.getAreaHarvested())
                .yieldPerUnit(yieldPerUnit)
                .marketPrice(request.getMarketPrice())
                .totalRevenue(totalRevenue)
                .qualityGrade(request.getQualityGrade())
                .storageLocation(request.getStorageLocation())
                .buyer(request.getBuyer())
                .notes(request.getNotes())
                .farmActivityId(farmActivityId)
        .patchId(request.getPatchId())
        .patchName(patch.getName())
                .build();
        
        YieldRecord saved = yieldRecordRepository.save(yieldRecord);
        return toResponse(saved);
    }

    public YieldRecordResponse getYieldRecord(Long userId, Long yieldRecordId) {
        YieldRecord yieldRecord = getOwnedYieldRecord(userId, yieldRecordId);
        return toResponse(yieldRecord);
    }

    @Transactional
    public YieldRecordResponse updateYieldRecord(Long userId, Long yieldRecordId, YieldRecordRequest request) {
        YieldRecord yieldRecord = getOwnedYieldRecord(userId, yieldRecordId);
    MaizePatch patch = maizePatchRepository.findById(request.getPatchId())
        .filter(p -> p.getFarmerProfileId().equals(userId))
        .orElseThrow(() -> new EntityNotFoundException("Patch not found or access denied"));
        Long farmActivityId = request.getFarmActivityId();
        
        BigDecimal yieldPerUnit = null;
        if (request.getAreaHarvested() != null && request.getAreaHarvested().compareTo(BigDecimal.ZERO) > 0) {
            yieldPerUnit = request.getYieldAmount().divide(request.getAreaHarvested(), 2, BigDecimal.ROUND_HALF_UP);
        }
        
        BigDecimal totalRevenue = null;
        if (request.getMarketPrice() != null) {
            totalRevenue = request.getYieldAmount().multiply(request.getMarketPrice());
        }
        
        yieldRecord.setCropType(request.getCropType());
        yieldRecord.setHarvestDate(request.getHarvestDate());
        yieldRecord.setYieldAmount(request.getYieldAmount());
        yieldRecord.setUnit(request.getUnit());
        yieldRecord.setAreaHarvested(request.getAreaHarvested());
        yieldRecord.setYieldPerUnit(yieldPerUnit);
        yieldRecord.setMarketPrice(request.getMarketPrice());
        yieldRecord.setTotalRevenue(totalRevenue);
        yieldRecord.setQualityGrade(request.getQualityGrade());
        yieldRecord.setStorageLocation(request.getStorageLocation());
        yieldRecord.setBuyer(request.getBuyer());
        yieldRecord.setNotes(request.getNotes());
        yieldRecord.setFarmActivityId(farmActivityId);
    yieldRecord.setPatchId(request.getPatchId());
    yieldRecord.setPatchName(patch.getName());
        
        YieldRecord saved = yieldRecordRepository.save(yieldRecord);
        return toResponse(saved);
    }

    @Transactional
    public void deleteYieldRecord(Long userId, Long yieldRecordId) {
        YieldRecord yieldRecord = getOwnedYieldRecord(userId, yieldRecordId);
        yieldRecordRepository.delete(yieldRecord);
    }

    public Page<YieldRecordResponse> listYieldRecords(Long userId, String cropType, Pageable pageable) {
        Page<YieldRecord> page;
        if (cropType != null) {
            page = yieldRecordRepository.findByFarmerProfileIdAndCropType(userId, cropType, pageable);
        } else {
            page = yieldRecordRepository.findByFarmerProfileId(userId, pageable);
        }
        return page.map(this::toResponse);
    }

    public BigDecimal getTotalYield(Long userId, String cropType) {
        if (cropType != null) {
            return yieldRecordRepository.getTotalYieldByFarmerProfileAndCrop(userId, cropType);
        }
        return yieldRecordRepository.findByFarmerProfileId(userId, Pageable.unpaged())
                .getContent()
                .stream()
                .map(YieldRecord::getYieldAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalRevenue(Long userId, String cropType) {
        if (cropType != null) {
            return yieldRecordRepository.getTotalRevenueByFarmerProfileAndCrop(userId, cropType);
        }
        return yieldRecordRepository.findByFarmerProfileId(userId, Pageable.unpaged())
                .getContent()
                .stream()
                .map(YieldRecord::getTotalRevenue)
                .filter(revenue -> revenue != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getAverageYieldPerUnit(Long userId, String cropType) {
        return yieldRecordRepository.getAverageYieldPerUnitByFarmerProfileAndCrop(userId, cropType);
    }

    public BigDecimal getBestYieldPerUnit(Long userId, String cropType) {
        return yieldRecordRepository.getBestYieldPerUnitByFarmerProfileAndCrop(userId, cropType);
    }

    public List<YieldRecordResponse> getYieldTrends(Long userId, String cropType, LocalDate startDate, LocalDate endDate) {
        List<YieldRecord> yieldRecords;
        if (startDate != null && endDate != null) {
            yieldRecords = yieldRecordRepository.findByFarmerProfileIdAndCropTypeAndHarvestDateBetweenOrderByHarvestDateAsc(
                    userId, cropType, startDate, endDate);
        } else {
            yieldRecords = yieldRecordRepository.findByFarmerProfileIdAndCropTypeOrderByHarvestDateAsc(userId, cropType);
        }
        return yieldRecords.stream().map(this::toResponse).toList();
    }

    private YieldRecord getOwnedYieldRecord(Long userId, Long yieldRecordId) {
        YieldRecord yieldRecord = yieldRecordRepository.findById(yieldRecordId)
                .orElseThrow(() -> new EntityNotFoundException("Yield record not found"));
        if (!yieldRecord.getFarmerProfileId().equals(userId)) {
            throw new SecurityException("Access denied");
        }
        return yieldRecord;
    }

    private YieldRecordResponse toResponse(YieldRecord yieldRecord) {
        YieldRecordResponse response = new YieldRecordResponse();
        response.setPatchId(yieldRecord.getPatchId());
        response.setPatchName(yieldRecord.getPatchName());
        response.setId(yieldRecord.getId());
        response.setCropType(yieldRecord.getCropType());
        response.setHarvestDate(yieldRecord.getHarvestDate());
        response.setYieldAmount(yieldRecord.getYieldAmount());
        response.setUnit(yieldRecord.getUnit());
        response.setAreaHarvested(yieldRecord.getAreaHarvested());
        response.setYieldPerUnit(yieldRecord.getYieldPerUnit());
        response.setMarketPrice(yieldRecord.getMarketPrice());
        response.setTotalRevenue(yieldRecord.getTotalRevenue());
        response.setQualityGrade(yieldRecord.getQualityGrade());
        response.setStorageLocation(yieldRecord.getStorageLocation());
        response.setBuyer(yieldRecord.getBuyer());
        response.setNotes(yieldRecord.getNotes());
        response.setFarmActivityId(yieldRecord.getFarmActivityId());
        response.setCreatedAt(yieldRecord.getCreatedAt());
        response.setUpdatedAt(yieldRecord.getUpdatedAt());
        return response;
    }
}
