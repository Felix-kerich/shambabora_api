package com.app.shambabora.modules.collaboration.dto;

import com.app.shambabora.modules.collaboration.entity.GroupMembership;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMembershipDTO {
    private Long id;
    private Long groupId;
    private String groupName;
    private Long userId;
    private String userName;
    private GroupMembership.MembershipRole role;
    private GroupMembership.MembershipStatus status;
    private Instant joinedAt;
    private Long invitedBy;
    private String invitedByName;
}
