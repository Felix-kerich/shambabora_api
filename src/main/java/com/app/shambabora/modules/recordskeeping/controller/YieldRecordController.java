package com.app.shambabora.modules.recordskeeping.controller;

import com.app.shambabora.modules.recordskeeping.dto.YieldRecordRequest;
import com.app.shambabora.modules.recordskeeping.dto.YieldRecordResponse;
import com.app.shambabora.entity.User;
import com.app.shambabora.modules.recordskeeping.service.YieldRecordService;
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
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/yield-records")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class YieldRecordController {
    
    private final YieldRecordService yieldRecordService;

    @Operation(summary = "Create a new yield record")
    @PostMapping
    public ResponseEntity<YieldRecordResponse> createYieldRecord(
            Authentication authentication,
            @Valid @RequestBody YieldRecordRequest request) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        return ResponseEntity.ok(yieldRecordService.createYieldRecord(userId, request));
    }

    @Operation(summary = "Get a yield record by ID")
    @GetMapping("/{id}")
    public ResponseEntity<YieldRecordResponse> getYieldRecord(
            Authentication authentication,
            @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        return ResponseEntity.ok(yieldRecordService.getYieldRecord(userId, id));
    }

    @Operation(summary = "Update a yield record")
    @PutMapping("/{id}")
    public ResponseEntity<YieldRecordResponse> updateYieldRecord(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody YieldRecordRequest request) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        return ResponseEntity.ok(yieldRecordService.updateYieldRecord(userId, id, request));
    }

    @Operation(summary = "Delete a yield record")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteYieldRecord(
            Authentication authentication,
            @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        yieldRecordService.deleteYieldRecord(userId, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List yield records with pagination and filtering")
    @GetMapping
    public ResponseEntity<Page<YieldRecordResponse>> listYieldRecords(
            Authentication authentication,
            @RequestParam(required = false) String cropType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(yieldRecordService.listYieldRecords(userId, cropType, pageable));
    }

    @Operation(summary = "Get total yield for a crop")
    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getTotalYield(
            Authentication authentication,
            @RequestParam(required = false) String cropType) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        return ResponseEntity.ok(yieldRecordService.getTotalYield(userId, cropType));
    }

    @Operation(summary = "Get total revenue for a crop")
    @GetMapping("/revenue")
    public ResponseEntity<BigDecimal> getTotalRevenue(
            Authentication authentication,
            @RequestParam(required = false) String cropType) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        return ResponseEntity.ok(yieldRecordService.getTotalRevenue(userId, cropType));
    }

    @Operation(summary = "Get average yield per unit for a crop")
    @GetMapping("/average")
    public ResponseEntity<BigDecimal> getAverageYieldPerUnit(
            Authentication authentication,
            @RequestParam String cropType) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        return ResponseEntity.ok(yieldRecordService.getAverageYieldPerUnit(userId, cropType));
    }

    @Operation(summary = "Get best yield per unit for a crop")
    @GetMapping("/best")
    public ResponseEntity<BigDecimal> getBestYieldPerUnit(
            Authentication authentication,
            @RequestParam String cropType) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        return ResponseEntity.ok(yieldRecordService.getBestYieldPerUnit(userId, cropType));
    }

    @Operation(summary = "Get yield trends for a crop")
    @GetMapping("/trends")
    public ResponseEntity<List<YieldRecordResponse>> getYieldTrends(
            Authentication authentication,
            @RequestParam String cropType,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        return ResponseEntity.ok(yieldRecordService.getYieldTrends(userId, cropType, startDate, endDate));
    }
}
