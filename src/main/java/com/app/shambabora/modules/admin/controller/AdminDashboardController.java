package com.app.shambabora.modules.admin.controller;

import com.app.shambabora.common.api.ApiResponse;
import com.app.shambabora.modules.admin.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'EXTENSION_OFFICER')")
public class AdminDashboardController {
    
    private final AdminDashboardService adminDashboardService;
    
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminDashboardService.DashboardStatsDTO>> getDashboardStats() {
        log.info("Fetching comprehensive dashboard statistics");
        return ResponseEntity.ok(adminDashboardService.getComprehensiveStats());
    }
}
