package com.app.shambabora.modules.recordskeeping.repository;

import com.app.shambabora.modules.recordskeeping.entity.YieldRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface YieldRecordRepository extends JpaRepository<YieldRecord, Long> {
    
    // Find yield records by farmer profile id
    Page<YieldRecord> findByFarmerProfileId(Long farmerProfileId, Pageable pageable);
    
    // Find yield records by farmer profile id and crop
    Page<YieldRecord> findByFarmerProfileIdAndCropType(Long farmerProfileId, String cropType, Pageable pageable);
    
    // Find yield records by farmer profile id and date range
    Page<YieldRecord> findByFarmerProfileIdAndHarvestDateBetween(Long farmerProfileId, LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    // Calculate total yield by farmer profile id and crop
    @Query("SELECT SUM(yr.yieldAmount) FROM YieldRecord yr WHERE yr.farmerProfileId = :farmerProfileId AND yr.cropType = :cropType")
    BigDecimal getTotalYieldByFarmerProfileAndCrop(@Param("farmerProfileId") Long farmerProfileId, @Param("cropType") String cropType);
    
    // Calculate total revenue by farmer profile id and crop
    @Query("SELECT SUM(yr.totalRevenue) FROM YieldRecord yr WHERE yr.farmerProfileId = :farmerProfileId AND yr.cropType = :cropType")
    BigDecimal getTotalRevenueByFarmerProfileAndCrop(@Param("farmerProfileId") Long farmerProfileId, @Param("cropType") String cropType);
    
    // Get average yield per unit by farmer profile id and crop
    @Query("SELECT AVG(yr.yieldPerUnit) FROM YieldRecord yr WHERE yr.farmerProfileId = :farmerProfileId AND yr.cropType = :cropType")
    BigDecimal getAverageYieldPerUnitByFarmerProfileAndCrop(@Param("farmerProfileId") Long farmerProfileId, @Param("cropType") String cropType);
    
    // Get best yield by farmer profile id and crop
    @Query("SELECT MAX(yr.yieldPerUnit) FROM YieldRecord yr WHERE yr.farmerProfileId = :farmerProfileId AND yr.cropType = :cropType")
    BigDecimal getBestYieldPerUnitByFarmerProfileAndCrop(@Param("farmerProfileId") Long farmerProfileId, @Param("cropType") String cropType);
    
    // Get yield records ordered by harvest date for trend analysis
    List<YieldRecord> findByFarmerProfileIdAndCropTypeOrderByHarvestDateAsc(Long farmerProfileId, String cropType);
    
    // Get yield records by farmer profile id, crop, and date range for trend analysis
    List<YieldRecord> findByFarmerProfileIdAndCropTypeAndHarvestDateBetweenOrderByHarvestDateAsc(Long farmerProfileId, String cropType, LocalDate startDate, LocalDate endDate);

    Optional<YieldRecord> findTopByFarmerProfileIdOrderByHarvestDateDesc(Long farmerProfileId);

    @Query("SELECT DISTINCT yr.cropType FROM YieldRecord yr WHERE yr.farmerProfileId = :farmerProfileId")
    List<String> findDistinctCropTypesByFarmerProfileId(@Param("farmerProfileId") Long farmerProfileId);
}
