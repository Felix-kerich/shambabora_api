package com.app.shambabora.modules.recordskeeping.controller;

import com.app.shambabora.entity.User;
import com.app.shambabora.modules.recordskeeping.dto.FarmAnalyticsResponse;
import com.app.shambabora.modules.recordskeeping.dto.FarmRecommendationResponse;
import com.app.shambabora.modules.recordskeeping.service.FarmRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import com.app.shambabora.modules.recordskeeping.dto.PatchSummaryDTO;
import com.app.shambabora.modules.recordskeeping.dto.PatchComparisonDTO;
import com.app.shambabora.modules.recordskeeping.service.PatchAnalyticsService;
import java.util.List;

@RestController
@RequestMapping("/api/farm-analytics")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class FarmAnalyticsController {

    private final FarmRecommendationService recommendationService;
    private final PatchAnalyticsService patchAnalyticsService;

    @Operation(summary = "Generate analytics for a crop and optional period")
    @GetMapping
    public ResponseEntity<FarmAnalyticsResponse> getAnalytics(
            Authentication authentication,
            @RequestParam(required = false) String cropType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        return ResponseEntity.ok(recommendationService.getAnalytics(userId, cropType, startDate, endDate));
    }

    @Operation(summary = "Generate AI-powered advice and fertilizer recommendations")
    @GetMapping("/advice")
    public ResponseEntity<FarmRecommendationResponse> getAdvice(
            Authentication authentication,
            @RequestParam(required = false) String cropType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        return ResponseEntity.ok(recommendationService.generateAdvice(userId, cropType, startDate, endDate));
    }

    @Operation(summary = "Get a summary for a specific patch/plot")
    @GetMapping("/patches/{patchId}/summary")
    public ResponseEntity<PatchSummaryDTO> getPatchSummary(Authentication authentication, @PathVariable Long patchId) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        return ResponseEntity.ok(patchAnalyticsService.getPatchSummary(userId, patchId));
    }

    @Operation(summary = "Compare multiple patches (by ids) for the authenticated farmer")
    @PostMapping("/patches/compare")
    public ResponseEntity<PatchComparisonDTO> comparePatches(Authentication authentication, @RequestBody List<Long> patchIds) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        return ResponseEntity.ok(patchAnalyticsService.comparePatches(userId, patchIds));
    }
}

