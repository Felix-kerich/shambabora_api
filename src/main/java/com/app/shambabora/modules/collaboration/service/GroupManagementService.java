package com.app.shambabora.modules.collaboration.service;

import com.app.shambabora.common.api.ApiResponse;
import com.app.shambabora.common.api.PageResponse;
import com.app.shambabora.common.exception.BadRequestException;
import com.app.shambabora.common.exception.NotFoundException;
import com.app.shambabora.entity.User;
import com.app.shambabora.modules.collaboration.dto.GroupDTO;
import com.app.shambabora.modules.collaboration.dto.GroupMembershipDTO;
import com.app.shambabora.modules.collaboration.entity.Group;
import com.app.shambabora.modules.collaboration.entity.GroupMembership;
import com.app.shambabora.modules.collaboration.repository.GroupMembershipRepository;
import com.app.shambabora.modules.collaboration.repository.GroupRepository;
import com.app.shambabora.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupManagementService {
    
    private final GroupRepository groupRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public ApiResponse<GroupDTO> createGroup(GroupDTO groupDTO, Long ownerId) {
        log.info("Creating group: {} by user: {}", groupDTO.getName(), ownerId);
        
        // Check if user already has 3 groups (max limit)
        long userGroupCount = groupMembershipRepository.countByUserIdAndStatusAndRole(
            ownerId, GroupMembership.MembershipStatus.ACTIVE, GroupMembership.MembershipRole.ADMIN);
        
        if (userGroupCount >= 3) {
            throw new BadRequestException("Maximum of 3 groups allowed per user");
        }
        
        Group group = Group.builder()
                .name(groupDTO.getName())
                .description(groupDTO.getDescription())
                .ownerId(ownerId)
                .build();
        
        Group savedGroup = groupRepository.save(group);
        
        // Add owner as admin
        GroupMembership ownerMembership = GroupMembership.builder()
                .groupId(savedGroup.getId())
                .userId(ownerId)
                .role(GroupMembership.MembershipRole.ADMIN)
                .status(GroupMembership.MembershipStatus.ACTIVE)
                .build();
        
        groupMembershipRepository.save(ownerMembership);
        log.info("Group created with ID: {} and owner added as admin", savedGroup.getId());
        
        return ApiResponse.ok("Group created successfully", mapToDTO(savedGroup));
    }
    
    @Transactional
    public ApiResponse<GroupMembershipDTO> addMember(Long groupId, Long userId, Long inviterId) {
        log.info("Adding user {} to group {} by {}", userId, groupId, inviterId);
        
        // Check if inviter has permission (system admin/EO or group admin/moderator)
        if (!hasAdminOrModeratorRole(groupId, inviterId) && !hasSystemAdminOrEORole(inviterId)) {
            throw new BadRequestException("Insufficient permissions to add members");
        }
        
        // Check if user is already a member
        if (groupMembershipRepository.existsByGroupIdAndUserIdAndStatus(groupId, userId, GroupMembership.MembershipStatus.ACTIVE)) {
            throw new BadRequestException("User is already a member of this group");
        }
        
        GroupMembership membership = GroupMembership.builder()
                .groupId(groupId)
                .userId(userId)
                .role(GroupMembership.MembershipRole.MEMBER)
                .status(GroupMembership.MembershipStatus.ACTIVE)
                .invitedBy(inviterId)
                .build();
        
        GroupMembership savedMembership = groupMembershipRepository.save(membership);
        log.info("User {} added to group {}", userId, groupId);
        
        return ApiResponse.ok("Member added successfully", mapMembershipToDTO(savedMembership));
    }
    
    @Transactional
    public ApiResponse<GroupMembershipDTO> removeMember(Long groupId, Long userId, Long removerId) {
        log.info("Removing user {} from group {} by {}", userId, groupId, removerId);
        
        // Check if remover has permission (system admin/EO or group admin/moderator)
        if (!hasAdminOrModeratorRole(groupId, removerId) && !hasSystemAdminOrEORole(removerId)) {
            throw new BadRequestException("Insufficient permissions to remove members");
        }
        
        GroupMembership membership = groupMembershipRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new NotFoundException("Membership not found"));
        
        membership.setStatus(GroupMembership.MembershipStatus.LEFT);
        groupMembershipRepository.save(membership);
        log.info("User {} removed from group {}", userId, groupId);
        
        return ApiResponse.ok("Member removed successfully", mapMembershipToDTO(membership));
    }
    
    @Transactional
    public ApiResponse<GroupMembershipDTO> updateMemberRole(Long groupId, Long userId, GroupMembership.MembershipRole newRole, Long updaterId) {
        log.info("Updating role of user {} in group {} to {} by {}", userId, groupId, newRole, updaterId);
        
        // Check if updater is admin
        if (!hasAdminRole(groupId, updaterId)) {
            throw new BadRequestException("Only admins can update member roles");
        }
        
        GroupMembership membership = groupMembershipRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new NotFoundException("Membership not found"));
        
        membership.setRole(newRole);
        GroupMembership savedMembership = groupMembershipRepository.save(membership);
        log.info("Role updated for user {} in group {}", userId, groupId);
        
        return ApiResponse.ok("Member role updated successfully", mapMembershipToDTO(savedMembership));
    }
    
    @Transactional
    public ApiResponse<GroupMembershipDTO> suspendMember(Long groupId, Long userId, Long suspenderId) {
        log.info("Suspending user {} from group {} by {}", userId, groupId, suspenderId);
        
        // Check if suspender has permission (system admin/EO or group admin/moderator)
        if (!hasAdminOrModeratorRole(groupId, suspenderId) && !hasSystemAdminOrEORole(suspenderId)) {
            throw new BadRequestException("Insufficient permissions to suspend members");
        }
        
        GroupMembership membership = groupMembershipRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new NotFoundException("Membership not found"));
        
        membership.setStatus(GroupMembership.MembershipStatus.SUSPENDED);
        GroupMembership savedMembership = groupMembershipRepository.save(membership);
        log.info("User {} suspended from group {}", userId, groupId);
        
        return ApiResponse.ok("Member suspended successfully", mapMembershipToDTO(savedMembership));
    }
    
    public ApiResponse<PageResponse<GroupMembershipDTO>> getGroupMembers(Long groupId, Long requesterId, Pageable pageable) {
        log.info("Getting members for group {} by user {}", groupId, requesterId);
        
        // Check if requester is a member
        if (!isGroupMember(groupId, requesterId)) {
            throw new BadRequestException("Not a member of this group");
        }
        
        List<GroupMembership> memberships = groupMembershipRepository.findByGroupIdAndStatus(groupId, GroupMembership.MembershipStatus.ACTIVE);
        
        List<GroupMembershipDTO> membershipDTOs = memberships.stream()
                .map(this::mapMembershipToDTO)
                .collect(Collectors.toList());
        
        PageResponse<GroupMembershipDTO> pageResponse = PageResponse.<GroupMembershipDTO>builder()
                .content(membershipDTOs)
                .page(0)
                .size(membershipDTOs.size())
                .totalElements(membershipDTOs.size())
                .totalPages(1)
                .build();
        
        return ApiResponse.ok("Group members retrieved successfully", pageResponse);
    }
    
    public ApiResponse<List<GroupDTO>> getUserGroups(Long userId) {
        log.info("Getting groups for user: {}", userId);
        
        List<Long> groupIds = groupMembershipRepository.findGroupIdsByUserIdAndStatus(userId, GroupMembership.MembershipStatus.ACTIVE);
        List<Group> groups = groupRepository.findAllById(groupIds);
        
        List<GroupDTO> groupDTOs = groups.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        return ApiResponse.ok("User groups retrieved successfully", groupDTOs);
    }
    
    @Transactional
    public ApiResponse<String> deleteGroup(Long groupId, Long requesterId) {
        log.info("Deleting group {} by user {}", groupId, requesterId);
        
        // Check if requester is admin
        if (!hasAdminRole(groupId, requesterId)) {
            throw new BadRequestException("Only admins can delete groups");
        }
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        
        // Remove all memberships
        groupMembershipRepository.deleteByGroupId(groupId);
        
        // Delete group
        groupRepository.delete(group);
        log.info("Group {} deleted", groupId);
        
        return ApiResponse.ok("Group deleted successfully", "Success");
    }
    
    public ApiResponse<PageResponse<GroupDTO>> browseGroups(String search, Long userId, Pageable pageable) {
        log.info("Browsing groups with search: {} by user {}", search, userId);
        
        Page<Group> groupPage;
        if (search != null && !search.trim().isEmpty()) {
            groupPage = groupRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
        } else {
            groupPage = groupRepository.findAll(pageable);
        }
        
        List<GroupDTO> groupDTOs = groupPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        PageResponse<GroupDTO> pageResponse = PageResponse.<GroupDTO>builder()
                .content(groupDTOs)
                .page(groupPage.getNumber())
                .size(groupPage.getSize())
                .totalElements(groupPage.getTotalElements())
                .totalPages(groupPage.getTotalPages())
                .build();
        
        return ApiResponse.ok("Groups retrieved successfully", pageResponse);
    }
    
    @Transactional
    public ApiResponse<GroupMembershipDTO> joinGroup(Long groupId, Long userId) {
        log.info("User {} joining group {}", userId, groupId);
        
        // Check if group exists
        groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        
        // Check if user is already a member
        if (groupMembershipRepository.existsByGroupIdAndUserIdAndStatus(groupId, userId, GroupMembership.MembershipStatus.ACTIVE)) {
            throw new BadRequestException("Already a member of this group");
        }
        
        GroupMembership membership = GroupMembership.builder()
                .groupId(groupId)
                .userId(userId)
                .role(GroupMembership.MembershipRole.MEMBER)
                .status(GroupMembership.MembershipStatus.ACTIVE)
                .build();
        
        GroupMembership savedMembership = groupMembershipRepository.save(membership);
        log.info("User {} joined group {}", userId, groupId);
        
        return ApiResponse.ok("Successfully joined group", mapMembershipToDTO(savedMembership));
    }
    
    @Transactional
    public ApiResponse<String> leaveGroup(Long groupId, Long userId) {
        log.info("User {} leaving group {}", userId, groupId);
        
        GroupMembership membership = groupMembershipRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new NotFoundException("Membership not found"));
        
        // Check if user is the owner
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        
        if (group.getOwnerId().equals(userId)) {
            throw new BadRequestException("Group owner cannot leave the group. Transfer ownership or delete the group.");
        }
        
        membership.setStatus(GroupMembership.MembershipStatus.LEFT);
        groupMembershipRepository.save(membership);
        log.info("User {} left group {}", userId, groupId);
        
        return ApiResponse.ok("Successfully left group", "Success");
    }
    
    public ApiResponse<GroupDTO> getGroup(Long groupId, Long userId) {
        log.info("Getting group {} details for user {}", groupId, userId);
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        
        // Add member count and user's membership status
        long memberCount = groupMembershipRepository.countByGroupIdAndStatus(groupId, GroupMembership.MembershipStatus.ACTIVE);
        boolean isMember = groupMembershipRepository.existsByGroupIdAndUserIdAndStatus(groupId, userId, GroupMembership.MembershipStatus.ACTIVE);
        List<Long> memberIds = groupMembershipRepository.findByGroupIdAndStatus(groupId, GroupMembership.MembershipStatus.ACTIVE)
                .stream()
                .map(GroupMembership::getUserId)
                .collect(Collectors.toList());
        String ownerName = getUserName(group.getOwnerId());
        
        GroupDTO groupDTO = GroupDTO.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .ownerId(group.getOwnerId())
                .ownerName(ownerName)
                .memberIds(memberIds)
                .memberCount(memberCount)
                .isMember(isMember)
                .createdAt(group.getCreatedAt())
                .build();
        
        return ApiResponse.ok("Group details retrieved successfully", groupDTO);
    }
    
    private boolean hasAdminRole(Long groupId, Long userId) {
        return groupMembershipRepository.findByGroupIdAndUserId(groupId, userId)
                .map(membership -> membership.getRole() == GroupMembership.MembershipRole.ADMIN)
                .orElse(false);
    }
    
    private boolean hasAdminOrModeratorRole(Long groupId, Long userId) {
        return groupMembershipRepository.findByGroupIdAndUserId(groupId, userId)
                .map(membership -> membership.getRole() == GroupMembership.MembershipRole.ADMIN || 
                                  membership.getRole() == GroupMembership.MembershipRole.MODERATOR)
                .orElse(false);
    }
    
    private boolean hasSystemAdminOrEORole(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getRoles().stream()
                        .anyMatch(role -> role.name().equals("ADMIN") || role.name().equals("EXTENSION_OFFICER")))
                .orElse(false);
    }
    
    private boolean isGroupMember(Long groupId, Long userId) {
        return groupMembershipRepository.existsByGroupIdAndUserIdAndStatus(groupId, userId, GroupMembership.MembershipStatus.ACTIVE);
    }
    
    private GroupDTO mapToDTO(Group group) {
        long memberCount = groupMembershipRepository.countByGroupIdAndStatus(group.getId(), GroupMembership.MembershipStatus.ACTIVE);
        List<Long> memberIds = groupMembershipRepository.findByGroupIdAndStatus(group.getId(), GroupMembership.MembershipStatus.ACTIVE)
                .stream()
                .map(GroupMembership::getUserId)
                .collect(Collectors.toList());
        String ownerName = getUserName(group.getOwnerId());
        
        return GroupDTO.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .ownerId(group.getOwnerId())
                .ownerName(ownerName)
                .memberIds(memberIds)
                .memberCount(memberCount)
                .createdAt(group.getCreatedAt())
                .build();
    }
    
    private GroupMembershipDTO mapMembershipToDTO(GroupMembership membership) {
        String userName = getUserName(membership.getUserId());
        String groupName = getGroupName(membership.getGroupId());
        String invitedByName = membership.getInvitedBy() != null ? getUserName(membership.getInvitedBy()) : null;
        
        return GroupMembershipDTO.builder()
                .id(membership.getId())
                .groupId(membership.getGroupId())
                .groupName(groupName)
                .userId(membership.getUserId())
                .userName(userName)
                .role(membership.getRole())
                .status(membership.getStatus())
                .joinedAt(membership.getJoinedAt())
                .invitedBy(membership.getInvitedBy())
                .invitedByName(invitedByName)
                .build();
    }
    
    private String getUserName(Long userId) {
        if (userId == null) return null;
        return userRepository.findById(userId)
                .map(User::getFullName)
                .orElse("Unknown User");
    }
    
    private String getGroupName(Long groupId) {
        if (groupId == null) return null;
        return groupRepository.findById(groupId)
                .map(Group::getName)
                .orElse("Unknown Group");
    }
    
    // ========== ADMIN-SPECIFIC METHODS ==========
    
    /**
     * Admin/EO creates a group without the 3-group limit
     */
    @Transactional
    public ApiResponse<GroupDTO> createGroupAsAdmin(GroupDTO groupDTO, Long adminId) {
        log.info("Admin/EO {} creating group: {}", adminId, groupDTO.getName());
        
        Group group = Group.builder()
                .name(groupDTO.getName())
                .description(groupDTO.getDescription())
                .ownerId(adminId)
                .build();
        
        Group savedGroup = groupRepository.save(group);
        
        // Add creator as admin
        GroupMembership ownerMembership = GroupMembership.builder()
                .groupId(savedGroup.getId())
                .userId(adminId)
                .role(GroupMembership.MembershipRole.ADMIN)
                .status(GroupMembership.MembershipStatus.ACTIVE)
                .build();
        
        groupMembershipRepository.save(ownerMembership);
        log.info("Admin created group with ID: {}", savedGroup.getId());
        
        return ApiResponse.ok("Group created successfully by admin", mapToDTO(savedGroup));
    }
    
    /**
     * Get all groups for admin view
     */
    public ApiResponse<PageResponse<GroupDTO>> getAllGroupsAdmin(String search, String status, Pageable pageable) {
        log.info("Admin fetching all groups");
        
        Page<Group> groupPage;
        
        if (search != null && !search.trim().isEmpty()) {
            groupPage = groupRepository.findByNameContainingIgnoreCase(search, pageable);
        } else {
            groupPage = groupRepository.findAll(pageable);
        }
        
        List<GroupDTO> groupDTOs = groupPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        PageResponse<GroupDTO> pageResponse = PageResponse.<GroupDTO>builder()
                .content(groupDTOs)
                .page(groupPage.getNumber())
                .size(groupPage.getSize())
                .totalElements(groupPage.getTotalElements())
                .totalPages(groupPage.getTotalPages())
                .build();
        
        return ApiResponse.ok("Groups retrieved successfully", pageResponse);
    }
    
    /**
     * Update group details (admin only)
     */
    @Transactional
    public ApiResponse<GroupDTO> updateGroupAsAdmin(Long groupId, GroupDTO groupDTO, Long adminId) {
        log.info("Admin {} updating group {}", adminId, groupId);
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        
        group.setName(groupDTO.getName());
        group.setDescription(groupDTO.getDescription());
        
        Group updatedGroup = groupRepository.save(group);
        log.info("Admin updated group {}", groupId);
        
        return ApiResponse.ok("Group updated successfully", mapToDTO(updatedGroup));
    }
    
    /**
     * Freeze or unfreeze a group
     */
    @Transactional
    public ApiResponse<GroupDTO> freezeGroup(Long groupId, boolean freeze, String reason, Long adminId) {
        log.info("Admin {} {} group {}", adminId, freeze ? "freezing" : "unfreezing", groupId);
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        
        // You can add a status field to Group entity if needed
        // For now, we'll update the description to indicate frozen status
        if (freeze) {
            group.setDescription(group.getDescription() + " [FROZEN: " + reason + "]");
        }
        
        Group updatedGroup = groupRepository.save(group);
        log.info("Group {} {}", groupId, freeze ? "frozen" : "unfrozen");
        
        return ApiResponse.ok("Group " + (freeze ? "frozen" : "unfrozen") + " successfully", mapToDTO(updatedGroup));
    }
    
    /**
     * Force delete a group (admin bypass)
     */
    @Transactional
    public ApiResponse<String> forceDeleteGroup(Long groupId, Long adminId) {
        log.info("Admin {} force deleting group {}", adminId, groupId);
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        
        // Delete all memberships first
        groupMembershipRepository.deleteAllByGroupId(groupId);
        
        // Delete the group
        groupRepository.delete(group);
        log.info("Admin force deleted group {}", groupId);
        
        return ApiResponse.ok("Group force deleted successfully", "Group has been permanently deleted");
    }
    
    /**
     * Get group statistics for admin dashboard
     */
    public ApiResponse<com.app.shambabora.modules.collaboration.controller.AdminGroupController.GroupStatsDTO> getGroupStatistics() {
        log.info("Fetching group statistics");
        
        long totalGroups = groupRepository.count();
        long activeGroups = totalGroups; // You can add logic to filter by status
        long frozenGroups = 0; // Implement based on your Group entity
        long totalMembers = groupMembershipRepository.countByStatus(GroupMembership.MembershipStatus.ACTIVE);
        long totalPosts = 0; // You can count posts if you have that relationship
        
        com.app.shambabora.modules.collaboration.controller.AdminGroupController.GroupStatsDTO stats = 
            new com.app.shambabora.modules.collaboration.controller.AdminGroupController.GroupStatsDTO(
                totalGroups, activeGroups, frozenGroups, totalMembers, totalPosts
            );
        
        return ApiResponse.ok("Group statistics retrieved successfully", stats);
    }
}
