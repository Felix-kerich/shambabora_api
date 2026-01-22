package com.app.shambabora.modules.collaboration.controller;

import com.app.shambabora.common.api.ApiResponse;
import com.app.shambabora.common.api.PageResponse;
import com.app.shambabora.modules.collaboration.dto.GroupDTO;
import com.app.shambabora.modules.collaboration.service.GroupManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Admin/Extension Officer specific endpoints for group management
 */
@RestController
@RequestMapping("/api/admin/collaboration/groups")
@RequiredArgsConstructor
@Slf4j
public class AdminGroupController {
    
    private final GroupManagementService groupManagementService;
    
    /**
     * Admin/EO creates a group (no limit on number of groups)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<GroupDTO>> createGroupAsAdmin(
            @Valid @RequestBody GroupDTO groupDTO,
            @RequestHeader("X-User-Id") Long adminId) {
        log.info("Admin/EO {} creating group: {}", adminId, groupDTO.getName());
        return ResponseEntity.ok(groupManagementService.createGroupAsAdmin(groupDTO, adminId));
    }
    
    /**
     * Get all groups (admin view with more details)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<GroupDTO>>> getAllGroups(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Admin fetching all groups with search: {} and status: {}", search, status);
        return ResponseEntity.ok(groupManagementService.getAllGroupsAdmin(search, status, pageable));
    }
    
    /**
     * Update group details (admin only)
     */
    @PutMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupDTO>> updateGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody GroupDTO groupDTO,
            @RequestHeader("X-User-Id") Long adminId) {
        log.info("Admin {} updating group {}", adminId, groupId);
        return ResponseEntity.ok(groupManagementService.updateGroupAsAdmin(groupId, groupDTO, adminId));
    }
    
    /**
     * Freeze/Unfreeze a group (admin only)
     */
    @PostMapping("/{groupId}/freeze")
    public ResponseEntity<ApiResponse<GroupDTO>> freezeGroup(
            @PathVariable Long groupId,
            @RequestParam boolean freeze,
            @RequestParam(required = false) String reason,
            @RequestHeader("X-User-Id") Long adminId) {
        log.info("Admin {} {} group {}", adminId, freeze ? "freezing" : "unfreezing", groupId);
        return ResponseEntity.ok(groupManagementService.freezeGroup(groupId, freeze, reason, adminId));
    }
    
    /**
     * Force delete a group (admin only - bypasses ownership check)
     */
    @DeleteMapping("/{groupId}/force")
    public ResponseEntity<ApiResponse<String>> forceDeleteGroup(
            @PathVariable Long groupId,
            @RequestHeader("X-User-Id") Long adminId) {
        log.info("Admin {} force deleting group {}", adminId, groupId);
        return ResponseEntity.ok(groupManagementService.forceDeleteGroup(groupId, adminId));
    }
    
    /**
     * Get group statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<GroupStatsDTO>> getGroupStatistics() {
        log.info("Admin fetching group statistics");
        return ResponseEntity.ok(groupManagementService.getGroupStatistics());
    }
    
    /**
     * DTO for group statistics
     */
    public static class GroupStatsDTO {
        private long totalGroups;
        private long activeGroups;
        private long frozenGroups;
        private long totalMembers;
        private long totalPosts;
        
        public GroupStatsDTO(long totalGroups, long activeGroups, long frozenGroups, long totalMembers, long totalPosts) {
            this.totalGroups = totalGroups;
            this.activeGroups = activeGroups;
            this.frozenGroups = frozenGroups;
            this.totalMembers = totalMembers;
            this.totalPosts = totalPosts;
        }
        
        // Getters and setters
        public long getTotalGroups() { return totalGroups; }
        public void setTotalGroups(long totalGroups) { this.totalGroups = totalGroups; }
        public long getActiveGroups() { return activeGroups; }
        public void setActiveGroups(long activeGroups) { this.activeGroups = activeGroups; }
        public long getFrozenGroups() { return frozenGroups; }
        public void setFrozenGroups(long frozenGroups) { this.frozenGroups = frozenGroups; }
        public long getTotalMembers() { return totalMembers; }
        public void setTotalMembers(long totalMembers) { this.totalMembers = totalMembers; }
        public long getTotalPosts() { return totalPosts; }
        public void setTotalPosts(long totalPosts) { this.totalPosts = totalPosts; }
    }
}
