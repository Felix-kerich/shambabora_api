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

    // Find expenses by patch id
    List<FarmExpense> findByPatchId(Long patchId);

    @Query("SELECT SUM(fe.amount) FROM FarmExpense fe WHERE fe.patchId = :patchId")
    java.math.BigDecimal getTotalExpensesByPatchId(@Param("patchId") Long patchId);

    // Get total expenses by patch id and category
    @Query("SELECT fe.category, SUM(fe.amount) FROM FarmExpense fe WHERE fe.patchId = :patchId GROUP BY fe.category")
    List<Object[]> getExpensesByCategoryByPatchId(@Param("patchId") Long patchId);

    // Get total expenses by patch id and growth stage
    @Query("SELECT fe.growthStage, SUM(fe.amount) FROM FarmExpense fe WHERE fe.patchId = :patchId GROUP BY fe.growthStage")
    List<Object[]> getExpensesByGrowthStageByPatchId(@Param("patchId") Long patchId);

    // Get expenses by patch id and category (for labour, fertiliser, pesticide, seeds)
    @Query("SELECT SUM(fe.amount) FROM FarmExpense fe WHERE fe.patchId = :patchId AND fe.category = :category")
    BigDecimal getTotalExpensesByPatchIdAndCategory(@Param("patchId") Long patchId, @Param("category") FarmExpense.ExpenseCategory category);

    // Find all patches for a farmer with their total expenses
    @Query("SELECT fe.patchId, fe.patchName, SUM(fe.amount) as totalExpense FROM FarmExpense fe WHERE fe.farmerProfileId = :farmerProfileId GROUP BY fe.patchId, fe.patchName ORDER BY totalExpense DESC")
    List<Object[]> getPatchExpenseSummaryForFarmer(@Param("farmerProfileId") Long farmerProfileId);

    // Get total labour costs for a patch
    @Query("SELECT SUM(fe.amount) FROM FarmExpense fe WHERE fe.patchId = :patchId AND fe.category = 'LABOR'")
    BigDecimal getTotalLabourCostByPatchId(@Param("patchId") Long patchId);

    // Get total fertiliser costs for a patch
    @Query("SELECT SUM(fe.amount) FROM FarmExpense fe WHERE fe.patchId = :patchId AND fe.category = 'FERTILIZER'")
    BigDecimal getTotalFertiliserCostByPatchId(@Param("patchId") Long patchId);

    // Get total pesticide costs for a patch
    @Query("SELECT SUM(fe.amount) FROM FarmExpense fe WHERE fe.patchId = :patchId AND fe.category = 'PESTICIDES'")
    BigDecimal getTotalPesticideCostByPatchId(@Param("patchId") Long patchId);

    // Get total seed costs for a patch
    @Query("SELECT SUM(fe.amount) FROM FarmExpense fe WHERE fe.patchId = :patchId AND fe.category = 'SEEDS'")
    BigDecimal getTotalSeedCostByPatchId(@Param("patchId") Long patchId);
}
