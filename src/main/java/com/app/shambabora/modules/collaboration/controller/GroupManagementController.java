package com.app.shambabora.modules.collaboration.controller;

import com.app.shambabora.common.api.ApiResponse;
import com.app.shambabora.common.api.PageResponse;
import com.app.shambabora.modules.collaboration.dto.GroupDTO;
import com.app.shambabora.modules.collaboration.dto.GroupMembershipDTO;
import com.app.shambabora.modules.collaboration.entity.GroupMembership;
import com.app.shambabora.modules.collaboration.service.GroupManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collaboration/groups")
@RequiredArgsConstructor
@Slf4j
public class GroupManagementController {
    
    private final GroupManagementService groupManagementService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<GroupDTO>> createGroup(@Valid @RequestBody GroupDTO groupDTO,
                                                            @RequestHeader("X-User-Id") Long ownerId) {
        log.info("Creating group: {} by user: {}", groupDTO.getName(), ownerId);
        return ResponseEntity.ok(groupManagementService.createGroup(groupDTO, ownerId));
    }
    
    @PostMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse<GroupMembershipDTO>> addMember(@PathVariable Long groupId,
                                                                    @RequestParam Long userId,
                                                                    @RequestHeader("X-User-Id") Long inviterId) {
        log.info("Adding user {} to group {} by {}", userId, groupId, inviterId);
        return ResponseEntity.ok(groupManagementService.addMember(groupId, userId, inviterId));
    }
    
    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<ApiResponse<GroupMembershipDTO>> removeMember(@PathVariable Long groupId,
                                                                       @PathVariable Long userId,
                                                                       @RequestHeader("X-User-Id") Long removerId) {
        log.info("Removing user {} from group {} by {}", userId, groupId, removerId);
        return ResponseEntity.ok(groupManagementService.removeMember(groupId, userId, removerId));
    }
    
    @PutMapping("/{groupId}/members/{userId}/role")
    public ResponseEntity<ApiResponse<GroupMembershipDTO>> updateMemberRole(@PathVariable Long groupId,
                                                                            @PathVariable Long userId,
                                                                            @RequestParam GroupMembership.MembershipRole role,
                                                                            @RequestHeader("X-User-Id") Long updaterId) {
        log.info("Updating role of user {} in group {} to {} by {}", userId, groupId, role, updaterId);
        return ResponseEntity.ok(groupManagementService.updateMemberRole(groupId, userId, role, updaterId));
    }
    
    @PostMapping("/{groupId}/members/{userId}/suspend")
    public ResponseEntity<ApiResponse<GroupMembershipDTO>> suspendMember(@PathVariable Long groupId,
                                                                        @PathVariable Long userId,
                                                                        @RequestHeader("X-User-Id") Long suspenderId) {
        log.info("Suspending user {} from group {} by {}", userId, groupId, suspenderId);
        return ResponseEntity.ok(groupManagementService.suspendMember(groupId, userId, suspenderId));
    }
    
    @GetMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse<PageResponse<GroupMembershipDTO>>> getGroupMembers(@PathVariable Long groupId,
                                                                                        @RequestHeader("X-User-Id") Long requesterId,
                                                                                        @PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting members for group {} by user {}", groupId, requesterId);
        return ResponseEntity.ok(groupManagementService.getGroupMembers(groupId, requesterId, pageable));
    }
    
    @GetMapping("/my-groups")
    public ResponseEntity<ApiResponse<List<GroupDTO>>> getUserGroups(@RequestHeader("X-User-Id") Long userId) {
        log.info("Getting groups for user: {}", userId);
        return ResponseEntity.ok(groupManagementService.getUserGroups(userId));
    }
    
    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponse<String>> deleteGroup(@PathVariable Long groupId,
                                                          @RequestHeader("X-User-Id") Long requesterId) {
        log.info("Deleting group {} by user {}", groupId, requesterId);
        return ResponseEntity.ok(groupManagementService.deleteGroup(groupId, requesterId));
    }
    
    @GetMapping("/browse")
    public ResponseEntity<ApiResponse<PageResponse<GroupDTO>>> browseGroups(
            @RequestParam(required = false) String search,
            @RequestHeader("X-User-Id") Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Browsing groups with search: {} by user {}", search, userId);
        return ResponseEntity.ok(groupManagementService.browseGroups(search, userId, pageable));
    }
    
    @PostMapping("/{groupId}/join")
    public ResponseEntity<ApiResponse<GroupMembershipDTO>> joinGroup(@PathVariable Long groupId,
                                                                    @RequestHeader("X-User-Id") Long userId) {
        log.info("User {} joining group {}", userId, groupId);
        return ResponseEntity.ok(groupManagementService.joinGroup(groupId, userId));
    }
    
    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<ApiResponse<String>> leaveGroup(@PathVariable Long groupId,
                                                         @RequestHeader("X-User-Id") Long userId) {
        log.info("User {} leaving group {}", userId, groupId);
        return ResponseEntity.ok(groupManagementService.leaveGroup(groupId, userId));
    }
    
    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupDTO>> getGroup(@PathVariable Long groupId,
                                                         @RequestHeader("X-User-Id") Long userId) {
        log.info("Getting group {} details for user {}", groupId, userId);
        return ResponseEntity.ok(groupManagementService.getGroup(groupId, userId));
    }
}
