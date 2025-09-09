package com.app.shambabora.modules.recordskeeping.controller;

import com.app.shambabora.modules.recordskeeping.dto.FarmDashboardResponse;
import com.app.shambabora.entity.User;
import com.app.shambabora.modules.recordskeeping.service.FarmDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/farm-dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class FarmDashboardController {
    
    private final FarmDashboardService farmDashboardService;

    @Operation(summary = "Get comprehensive farm dashboard with all key metrics and recent activities")
    @GetMapping
    public ResponseEntity<FarmDashboardResponse> getDashboard(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        return ResponseEntity.ok(farmDashboardService.generateDashboard(userId));
    }
}
