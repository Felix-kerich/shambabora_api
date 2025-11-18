package com.app.shambabora.modules.recordskeeping.repository;

import com.app.shambabora.modules.recordskeeping.entity.InputUsageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InputUsageRecordRepository extends JpaRepository<InputUsageRecord, Long> {
    
    List<InputUsageRecord> findByFarmerProfileId(Long farmerProfileId);
    
    List<InputUsageRecord> findByFarmActivityId(Long farmActivityId);
    
    List<InputUsageRecord> findByYieldRecordId(Long yieldRecordId);
    
    List<InputUsageRecord> findByInputType(InputUsageRecord.InputType inputType);
    
    List<InputUsageRecord> findBySeedVarietyId(Long seedVarietyId);
    
    List<InputUsageRecord> findByFertilizerProductId(Long fertilizerProductId);
    
    List<InputUsageRecord> findByPesticideProductId(Long pesticideProductId);
    
    // Find all inputs used in a specific activity
    @Query("SELECT i FROM InputUsageRecord i WHERE i.farmActivityId = :activityId")
    List<InputUsageRecord> findInputsByActivity(@Param("activityId") Long activityId);
    
    // Find all inputs that contributed to a yield
    @Query("SELECT i FROM InputUsageRecord i WHERE i.yieldRecordId = :yieldId")
    List<InputUsageRecord> findInputsByYield(@Param("yieldId") Long yieldId);
    
    // Find inputs with high effectiveness ratings
    @Query("SELECT i FROM InputUsageRecord i WHERE i.farmerProfileId = :farmerId AND i.effectivenessRating >= :minRating ORDER BY i.effectivenessRating DESC")
    List<InputUsageRecord> findHighEffectiveInputs(@Param("farmerId") Long farmerId, @Param("minRating") Integer minRating);
    
    // Correlation analysis: Find yield records linked to specific seed varieties
    @Query("SELECT ir.yieldRecordId FROM InputUsageRecord ir WHERE ir.seedVarietyId = :seedId GROUP BY ir.yieldRecordId")
    List<Long> findYieldsForSeed(@Param("seedId") Long seedId);

    // Find inputs by patch id
    List<InputUsageRecord> findByPatchId(Long patchId);
}
