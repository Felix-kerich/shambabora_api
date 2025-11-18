package com.app.shambabora.modules.recordskeeping.repository;

import com.app.shambabora.modules.recordskeeping.entity.PesticideProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PesticideProductRepository extends JpaRepository<PesticideProduct, Long> {
    
    List<PesticideProduct> findByFarmerProfileIdAndIsActive(Long farmerProfileId, Boolean isActive);
    
    Optional<PesticideProduct> findByIdAndFarmerProfileId(Long id, Long farmerProfileId);
    
    @Query("SELECT p FROM PesticideProduct p WHERE p.farmerProfileId = :farmerId AND p.pesticideType = :type AND p.isActive = true ORDER BY p.effectivenessRating DESC NULLS LAST")
    List<PesticideProduct> findMostEffectivePesticides(@Param("farmerId") Long farmerId, @Param("type") PesticideProduct.PesticideType type);
    
    @Query("SELECT p FROM PesticideProduct p WHERE p.farmerProfileId = :farmerId AND p.isRecommendedOrganic = true AND p.isActive = true")
    List<PesticideProduct> findOrganicPesticides(@Param("farmerId") Long farmerId);
}
