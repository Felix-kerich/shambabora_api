package com.app.shambabora.modules.collaboration.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "group_memberships", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"group_id", "user_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMembership {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "group_id", nullable = false)
    private Long groupId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipRole role = MembershipRole.MEMBER;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipStatus status = MembershipStatus.ACTIVE;
    
    @Column(name = "joined_at")
    @CreationTimestamp
    private Instant joinedAt;
    
    @Column(name = "invited_by")
    private Long invitedBy;
    
    public enum MembershipRole {
        ADMIN,
        MODERATOR,
        OWNER,
        MEMBER
    }
    
    public enum MembershipStatus {
        ACTIVE,
        PENDING,
        SUSPENDED,
        LEFT
    }
}
