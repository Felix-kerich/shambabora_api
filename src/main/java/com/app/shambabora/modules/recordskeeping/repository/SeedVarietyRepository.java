package com.app.shambabora.modules.recordskeeping.repository;

import com.app.shambabora.modules.recordskeeping.entity.SeedVariety;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeedVarietyRepository extends JpaRepository<SeedVariety, Long> {
    
    List<SeedVariety> findByFarmerProfileIdAndCropType(Long farmerProfileId, String cropType);
    
    List<SeedVariety> findByFarmerProfileIdAndIsActive(Long farmerProfileId, Boolean isActive);
    
    Optional<SeedVariety> findByIdAndFarmerProfileId(Long id, Long farmerProfileId);
    
    @Query("SELECT s FROM SeedVariety s WHERE s.farmerProfileId = :farmerId AND s.cropType = :cropType ORDER BY s.yieldRating DESC NULLS LAST")
    List<SeedVariety> findTopSeedsByFarmerAndCrop(@Param("farmerId") Long farmerId, @Param("cropType") String cropType);
    
    @Query("SELECT s FROM SeedVariety s WHERE s.farmerProfileId = :farmerId AND s.isActive = true ORDER BY s.createdAt DESC")
    List<SeedVariety> findActiveSeedsForFarmer(@Param("farmerId") Long farmerId);
}
