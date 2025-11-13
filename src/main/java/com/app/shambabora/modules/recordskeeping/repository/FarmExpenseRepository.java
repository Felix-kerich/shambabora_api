package com.app.shambabora.modules.recordskeeping.repository;

import com.app.shambabora.modules.recordskeeping.entity.FarmExpense;
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
public interface FarmExpenseRepository extends JpaRepository<FarmExpense, Long> {
    
    // Find expenses by farmer profile id
    Page<FarmExpense> findByFarmerProfileId(Long farmerProfileId, Pageable pageable);
    
    // Find expenses by farmer profile id and crop
    Page<FarmExpense> findByFarmerProfileIdAndCropType(Long farmerProfileId, String cropType, Pageable pageable);
    
    // Find expenses by farmer profile id and category
    Page<FarmExpense> findByFarmerProfileIdAndCategory(Long farmerProfileId, FarmExpense.ExpenseCategory category, Pageable pageable);
    
    // Find expenses by farmer profile id, crop, and category
    Page<FarmExpense> findByFarmerProfileIdAndCropTypeAndCategory(Long farmerProfileId, String cropType, FarmExpense.ExpenseCategory category, Pageable pageable);
    
    // Find expenses by farmer profile id and date range
    Page<FarmExpense> findByFarmerProfileIdAndExpenseDateBetween(Long farmerProfileId, LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    // Find expenses by farmer profile id, crop, and growth stage
    List<FarmExpense> findByFarmerProfileIdAndCropTypeAndGrowthStage(Long farmerProfileId, String cropType, FarmExpense.GrowthStage growthStage);
    
    // Calculate total expenses by farmer profile id and crop
    @Query("SELECT SUM(fe.amount) FROM FarmExpense fe WHERE fe.farmerProfileId = :farmerProfileId AND fe.cropType = :cropType")
    BigDecimal getTotalExpensesByFarmerProfileAndCrop(@Param("farmerProfileId") Long farmerProfileId, @Param("cropType") String cropType);
    
    // Calculate total expenses by farmer profile id, crop, and date range
    @Query("SELECT SUM(fe.amount) FROM FarmExpense fe WHERE fe.farmerProfileId = :farmerProfileId AND fe.cropType = :cropType AND fe.expenseDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalExpensesByFarmerProfileCropAndDateRange(@Param("farmerProfileId") Long farmerProfileId, @Param("cropType") String cropType, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // Get expenses by category for a farmer profile id and crop
    @Query("SELECT fe.category, SUM(fe.amount) FROM FarmExpense fe WHERE fe.farmerProfileId = :farmerProfileId AND fe.cropType = :cropType GROUP BY fe.category")
    List<Object[]> getExpensesByCategoryForFarmerProfileAndCrop(@Param("farmerProfileId") Long farmerProfileId, @Param("cropType") String cropType);
    
    // Get expenses by growth stage for a farmer profile id and crop
    @Query("SELECT fe.growthStage, SUM(fe.amount) FROM FarmExpense fe WHERE fe.farmerProfileId = :farmerProfileId AND fe.cropType = :cropType GROUP BY fe.growthStage")
    List<Object[]> getExpensesByGrowthStageForFarmerProfileAndCrop(@Param("farmerProfileId") Long farmerProfileId, @Param("cropType") String cropType);

    @Query("SELECT DISTINCT fe.cropType FROM FarmExpense fe WHERE fe.farmerProfileId = :farmerProfileId")
    List<String> findDistinctCropTypesByFarmerProfileId(@Param("farmerProfileId") Long farmerProfileId);

    Optional<FarmExpense> findTopByFarmerProfileIdOrderByExpenseDateDesc(Long farmerProfileId);
}
