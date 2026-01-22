package com.app.shambabora.modules.collaboration.repository;

import com.app.shambabora.modules.collaboration.entity.GroupMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Long> {
    
    Optional<GroupMembership> findByGroupIdAndUserId(Long groupId, Long userId);
    
    List<GroupMembership> findByUserIdAndStatus(Long userId, GroupMembership.MembershipStatus status);
    
    List<GroupMembership> findByGroupIdAndStatus(Long groupId, GroupMembership.MembershipStatus status);
    
    List<GroupMembership> findByGroupIdAndRole(Long groupId, GroupMembership.MembershipRole role);
    
    boolean existsByGroupIdAndUserIdAndStatus(Long groupId, Long userId, GroupMembership.MembershipStatus status);
    
    @Query("SELECT gm.userId FROM GroupMembership gm WHERE gm.groupId = :groupId AND gm.status = :status")
    List<Long> findUserIdsByGroupIdAndStatus(@Param("groupId") Long groupId, @Param("status") GroupMembership.MembershipStatus status);
    
    @Query("SELECT gm.groupId FROM GroupMembership gm WHERE gm.userId = :userId AND gm.status = :status")
    List<Long> findGroupIdsByUserIdAndStatus(@Param("userId") Long userId, @Param("status") GroupMembership.MembershipStatus status);
    
    
    void deleteByGroupId(Long groupId);
    
    @Query("SELECT gm FROM GroupMembership gm WHERE gm.groupId = :groupId AND gm.role IN :roles")
    List<GroupMembership> findByGroupIdAndRoleIn(@Param("groupId") Long groupId, @Param("roles") List<GroupMembership.MembershipRole> roles);
    
    long countByUserIdAndStatusAndRole(Long userId, GroupMembership.MembershipStatus status, GroupMembership.MembershipRole role);
    
    long countByGroupIdAndStatus(Long groupId, GroupMembership.MembershipStatus status);
    
    long countByStatus(GroupMembership.MembershipStatus status);
    
    void deleteAllByGroupId(Long groupId);
}
