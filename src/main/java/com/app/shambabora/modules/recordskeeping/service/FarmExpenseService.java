package com.app.shambabora.modules.recordskeeping.service;

import com.app.shambabora.modules.recordskeeping.dto.FarmExpenseRequest;
import com.app.shambabora.modules.recordskeeping.dto.FarmExpenseResponse;
import com.app.shambabora.modules.recordskeeping.entity.FarmExpense;
import com.app.shambabora.modules.recordskeeping.repository.FarmExpenseRepository;
import com.app.shambabora.modules.recordskeeping.repository.MaizePatchRepository;
import com.app.shambabora.modules.recordskeeping.entity.MaizePatch;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FarmExpenseService {
    
    private final FarmExpenseRepository farmExpenseRepository;
    private final MaizePatchRepository maizePatchRepository;

    @Transactional
    public FarmExpenseResponse createExpense(Long userId, FarmExpenseRequest request) {
        // Here userId is treated as farmerProfileId since FarmerProfile entity is not available
    MaizePatch patch = maizePatchRepository.findById(request.getPatchId())
        .filter(p -> p.getFarmerProfileId().equals(userId))
        .orElseThrow(() -> new EntityNotFoundException("Patch not found or access denied"));

    FarmExpense expense = FarmExpense.builder()
                .farmerProfileId(userId)
                .cropType(request.getCropType())
                .category(FarmExpense.ExpenseCategory.valueOf(request.getCategory().toUpperCase()))
                .description(request.getDescription())
                .amount(request.getAmount())
                .expenseDate(request.getExpenseDate())
                .supplier(request.getSupplier())
                .invoiceNumber(request.getInvoiceNumber())
                .paymentMethod(request.getPaymentMethod())
                .notes(request.getNotes())
        .patchId(request.getPatchId())
        .patchName(patch.getName())
                .growthStage(request.getGrowthStage() != null ? 
                    FarmExpense.GrowthStage.valueOf(request.getGrowthStage().toUpperCase()) : null)
                .farmActivityId(request.getFarmActivityId())
                .isRecurring(request.getIsRecurring())
                .recurringFrequency(request.getRecurringFrequency())
                .build();
        
        FarmExpense saved = farmExpenseRepository.save(expense);
        return toResponse(saved);
    }

    public FarmExpenseResponse getExpense(Long userId, Long expenseId) {
        FarmExpense expense = getOwnedExpense(userId, expenseId);
        return toResponse(expense);
    }

    @Transactional
    public FarmExpenseResponse updateExpense(Long userId, Long expenseId, FarmExpenseRequest request) {
        FarmExpense expense = getOwnedExpense(userId, expenseId);
    MaizePatch patch = maizePatchRepository.findById(request.getPatchId())
        .filter(p -> p.getFarmerProfileId().equals(userId))
        .orElseThrow(() -> new EntityNotFoundException("Patch not found or access denied"));
        
        expense.setCropType(request.getCropType());
        expense.setCategory(FarmExpense.ExpenseCategory.valueOf(request.getCategory().toUpperCase()));
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setSupplier(request.getSupplier());
        expense.setInvoiceNumber(request.getInvoiceNumber());
        expense.setPaymentMethod(request.getPaymentMethod());
        expense.setNotes(request.getNotes());
        expense.setPatchId(request.getPatchId());
        expense.setPatchName(patch.getName());
        expense.setGrowthStage(request.getGrowthStage() != null ? 
            FarmExpense.GrowthStage.valueOf(request.getGrowthStage().toUpperCase()) : null);
        expense.setFarmActivityId(request.getFarmActivityId());
        expense.setIsRecurring(request.getIsRecurring());
        expense.setRecurringFrequency(request.getRecurringFrequency());
        
        FarmExpense saved = farmExpenseRepository.save(expense);
        return toResponse(saved);
    }

    @Transactional
    public void deleteExpense(Long userId, Long expenseId) {
        FarmExpense expense = getOwnedExpense(userId, expenseId);
        farmExpenseRepository.delete(expense);
    }

    public Page<FarmExpenseResponse> listExpenses(Long userId, String cropType, String category, Pageable pageable) {
        Page<FarmExpense> page;
        
        if (cropType != null && category != null) {
            page = farmExpenseRepository.findByFarmerProfileIdAndCropTypeAndCategory(
                    userId, cropType, FarmExpense.ExpenseCategory.valueOf(category.toUpperCase()), pageable);
        } else if (cropType != null) {
            page = farmExpenseRepository.findByFarmerProfileIdAndCropType(userId, cropType, pageable);
        } else if (category != null) {
            page = farmExpenseRepository.findByFarmerProfileIdAndCategory(
                    userId, FarmExpense.ExpenseCategory.valueOf(category.toUpperCase()), pageable);
        } else {
            page = farmExpenseRepository.findByFarmerProfileId(userId, pageable);
        }
        
        return page.map(this::toResponse);
    }

    public BigDecimal getTotalExpenses(Long userId, String cropType) {
        if (cropType != null) {
            return farmExpenseRepository.getTotalExpensesByFarmerProfileAndCrop(userId, cropType);
        }
        return farmExpenseRepository.findByFarmerProfileId(userId, Pageable.unpaged())
                .getContent()
                .stream()
                .map(FarmExpense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<String, BigDecimal> getExpensesByCategory(Long userId, String cropType) {
        List<Object[]> results = farmExpenseRepository.getExpensesByCategoryForFarmerProfileAndCrop(userId, cropType);
        return results.stream()
                .collect(Collectors.toMap(
                        result -> result[0].toString(),
                        result -> (BigDecimal) result[1]
                ));
    }

    public Map<String, BigDecimal> getExpensesByGrowthStage(Long userId, String cropType) {
        List<Object[]> results = farmExpenseRepository.getExpensesByGrowthStageForFarmerProfileAndCrop(userId, cropType);
        return results.stream()
                .collect(Collectors.toMap(
                        result -> result[0] != null ? result[0].toString() : "UNKNOWN",
                        result -> (BigDecimal) result[1]
                ));
    }

    private FarmExpense getOwnedExpense(Long userId, Long expenseId) {
        FarmExpense expense = farmExpenseRepository.findById(expenseId)
                .orElseThrow(() -> new EntityNotFoundException("Farm expense not found"));
        if (!expense.getFarmerProfileId().equals(userId)) {
            throw new SecurityException("Access denied");
        }
        return expense;
    }

    private FarmExpenseResponse toResponse(FarmExpense expense) {
        FarmExpenseResponse response = new FarmExpenseResponse();
        response.setPatchId(expense.getPatchId());
        response.setPatchName(expense.getPatchName());
        response.setId(expense.getId());
        response.setCropType(expense.getCropType());
        response.setCategory(expense.getCategory().name());
        response.setDescription(expense.getDescription());
        response.setAmount(expense.getAmount());
        response.setExpenseDate(expense.getExpenseDate());
        response.setSupplier(expense.getSupplier());
        response.setInvoiceNumber(expense.getInvoiceNumber());
        response.setPaymentMethod(expense.getPaymentMethod());
        response.setNotes(expense.getNotes());
        response.setGrowthStage(expense.getGrowthStage() != null ? expense.getGrowthStage().name() : null);
        response.setFarmActivityId(expense.getFarmActivityId());
        response.setIsRecurring(expense.getIsRecurring());
        response.setRecurringFrequency(expense.getRecurringFrequency());
        response.setCreatedAt(expense.getCreatedAt());
        response.setUpdatedAt(expense.getUpdatedAt());
        return response;
    }
}
