package com.app.shambabora.modules.recordskeeping.controller;

import com.app.shambabora.modules.recordskeeping.dto.FarmExpenseRequest;
import com.app.shambabora.modules.recordskeeping.dto.FarmExpenseResponse;
import com.app.shambabora.entity.User;
import com.app.shambabora.modules.recordskeeping.service.FarmExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/farm-expenses")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class FarmExpenseController {
    
    private final FarmExpenseService farmExpenseService;

    @Operation(summary = "Create a new farm expense")
    @PostMapping
    public ResponseEntity<FarmExpenseResponse> createExpense(
            Authentication authentication,
            @Valid @RequestBody FarmExpenseRequest request) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        return ResponseEntity.ok(farmExpenseService.createExpense(userId, request));
    }

    @Operation(summary = "Get a farm expense by ID")
    @GetMapping("/{id}")
    public ResponseEntity<FarmExpenseResponse> getExpense(
            Authentication authentication,
            @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        return ResponseEntity.ok(farmExpenseService.getExpense(userId, id));
    }

    @Operation(summary = "Update a farm expense")
    @PutMapping("/{id}")
    public ResponseEntity<FarmExpenseResponse> updateExpense(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody FarmExpenseRequest request) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        return ResponseEntity.ok(farmExpenseService.updateExpense(userId, id, request));
    }

    @Operation(summary = "Delete a farm expense")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(
            Authentication authentication,
            @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        farmExpenseService.deleteExpense(userId, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List farm expenses with pagination and filtering")
    @GetMapping
    public ResponseEntity<Page<FarmExpenseResponse>> listExpenses(
            Authentication authentication,
            @RequestParam(required = false) String cropType,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(farmExpenseService.listExpenses(userId, cropType, category, pageable));
    }

    @Operation(summary = "Get total expenses for a crop")
    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getTotalExpenses(
            Authentication authentication,
            @RequestParam(required = false) String cropType) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        return ResponseEntity.ok(farmExpenseService.getTotalExpenses(userId, cropType));
    }

    @Operation(summary = "Get expenses breakdown by category")
    @GetMapping("/breakdown/category")
    public ResponseEntity<Map<String, BigDecimal>> getExpensesByCategory(
            Authentication authentication,
            @RequestParam String cropType) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        return ResponseEntity.ok(farmExpenseService.getExpensesByCategory(userId, cropType));
    }

    @Operation(summary = "Get expenses breakdown by growth stage")
    @GetMapping("/breakdown/growth-stage")
    public ResponseEntity<Map<String, BigDecimal>> getExpensesByGrowthStage(
            Authentication authentication,
            @RequestParam String cropType) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        return ResponseEntity.ok(farmExpenseService.getExpensesByGrowthStage(userId, cropType));
    }
}
