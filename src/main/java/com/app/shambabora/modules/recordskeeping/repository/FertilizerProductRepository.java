package com.app.shambabora.modules.recordskeeping.repository;

import com.app.shambabora.modules.recordskeeping.entity.FertilizerProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FertilizerProductRepository extends JpaRepository<FertilizerProduct, Long> {
    
    List<FertilizerProduct> findByFarmerProfileIdAndIsActive(Long farmerProfileId, Boolean isActive);
    
    Optional<FertilizerProduct> findByIdAndFarmerProfileId(Long id, Long farmerProfileId);
    
    @Query("SELECT f FROM FertilizerProduct f WHERE f.farmerProfileId = :farmerId AND f.isActive = true ORDER BY f.effectivenessRating DESC NULLS LAST")
    List<FertilizerProduct> findMostEffectiveFertilizers(@Param("farmerId") Long farmerId);
    
    @Query("SELECT f FROM FertilizerProduct f WHERE f.farmerProfileId = :farmerId AND f.isCostEffective = true AND f.isActive = true")
    List<FertilizerProduct> findCostEffectiveFertilizers(@Param("farmerId") Long farmerId);
}
