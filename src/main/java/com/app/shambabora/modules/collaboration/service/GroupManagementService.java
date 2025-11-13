package com.app.shambabora.modules.collaboration.service;

import com.app.shambabora.common.api.ApiResponse;
import com.app.shambabora.common.api.PageResponse;
import com.app.shambabora.common.exception.BadRequestException;
import com.app.shambabora.common.exception.NotFoundException;
import com.app.shambabora.modules.collaboration.dto.GroupDTO;
import com.app.shambabora.modules.collaboration.dto.GroupMembershipDTO;
import com.app.shambabora.modules.collaboration.entity.Group;
import com.app.shambabora.modules.collaboration.entity.GroupMembership;
import com.app.shambabora.modules.collaboration.repository.GroupMembershipRepository;
import com.app.shambabora.modules.collaboration.repository.GroupRepository;
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
        
        // Check if inviter has permission
        if (!hasAdminOrModeratorRole(groupId, inviterId)) {
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
        
        // Check if remover has permission
        if (!hasAdminOrModeratorRole(groupId, removerId)) {
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
        
        // Check if suspender has permission
        if (!hasAdminOrModeratorRole(groupId, suspenderId)) {
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
        
        GroupDTO groupDTO = GroupDTO.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .ownerId(group.getOwnerId())
                .createdAt(group.getCreatedAt())
                .memberCount(memberCount)
                .isMember(isMember)
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
    
    private boolean isGroupMember(Long groupId, Long userId) {
        return groupMembershipRepository.existsByGroupIdAndUserIdAndStatus(groupId, userId, GroupMembership.MembershipStatus.ACTIVE);
    }
    
    private GroupDTO mapToDTO(Group group) {
        return GroupDTO.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .ownerId(group.getOwnerId())
                .createdAt(group.getCreatedAt())
                .build();
    }
    
    private GroupMembershipDTO mapMembershipToDTO(GroupMembership membership) {
        return GroupMembershipDTO.builder()
                .id(membership.getId())
                .groupId(membership.getGroupId())
                .userId(membership.getUserId())
                .role(membership.getRole())
                .status(membership.getStatus())
                .joinedAt(membership.getJoinedAt())
                .invitedBy(membership.getInvitedBy())
                .build();
    }
}
