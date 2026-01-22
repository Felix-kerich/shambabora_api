package com.app.shambabora.modules.admin.service;

import com.app.shambabora.common.api.ApiResponse;
import com.app.shambabora.modules.collaboration.repository.GroupMembershipRepository;
import com.app.shambabora.modules.collaboration.repository.GroupRepository;
import com.app.shambabora.modules.collaboration.repository.PostCommentRepository;
import com.app.shambabora.modules.collaboration.repository.PostRepository;
import com.app.shambabora.modules.collaboration.entity.GroupMembership;
import com.app.shambabora.modules.collaboration.entity.Post;
import com.app.shambabora.modules.collaboration.entity.PostComment;
import com.app.shambabora.modules.marketplace.repository.ProductRepository;
import com.app.shambabora.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardService {
    
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final GroupRepository groupRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final ProductRepository productRepository;
    
    public ApiResponse<DashboardStatsDTO> getComprehensiveStats() {
        log.info("Calculating comprehensive dashboard statistics");
        
        DashboardStatsDTO stats = new DashboardStatsDTO();
        
        // User Statistics
        stats.setTotalUsers(userRepository.count());
        stats.setActiveUsers(userRepository.findAll().stream()
                .filter(user -> user.getIsActive() != null && user.getIsActive())
                .count());
        
        // Post Statistics
        stats.setTotalPosts(postRepository.count());
        stats.setPendingPosts(postRepository.countByStatus(Post.PostStatus.PENDING_MODERATION));
        stats.setApprovedPosts(postRepository.countByStatus(Post.PostStatus.APPROVED));
        stats.setRejectedPosts(postRepository.countByStatus(Post.PostStatus.REJECTED));
        stats.setHiddenPosts(postRepository.countByStatus(Post.PostStatus.HIDDEN));
        
        // Comment Statistics
        stats.setTotalComments(postCommentRepository.count());
        stats.setPendingComments(postCommentRepository.countByStatus(PostComment.CommentStatus.PENDING_MODERATION));
        stats.setApprovedComments(postCommentRepository.countByStatus(PostComment.CommentStatus.APPROVED));
        stats.setRejectedComments(postCommentRepository.countByStatus(PostComment.CommentStatus.REJECTED));
        stats.setHiddenComments(postCommentRepository.countByStatus(PostComment.CommentStatus.HIDDEN));
        
        // Group Statistics
        stats.setTotalGroups(groupRepository.count());
        stats.setActiveGroups(groupRepository.count()); // All groups are considered active unless deleted
        stats.setFrozenGroups(0); // No frozen status in current implementation
        stats.setTotalGroupMembers(groupMembershipRepository.countByStatus(GroupMembership.MembershipStatus.ACTIVE));
        
        // Product Statistics
        stats.setTotalProducts(productRepository.count());
        stats.setAvailableProducts(productRepository.findAll().stream()
                .filter(product -> product.isAvailable())
                .count());
        
        log.info("Dashboard statistics calculated successfully");
        return ApiResponse.ok("Dashboard statistics retrieved successfully", stats);
    }
    
    @Data
    public static class DashboardStatsDTO {
        // User Stats
        private long totalUsers;
        private long activeUsers;
        
        // Post Stats
        private long totalPosts;
        private long pendingPosts;
        private long approvedPosts;
        private long rejectedPosts;
        private long hiddenPosts;
        
        // Comment Stats
        private long totalComments;
        private long pendingComments;
        private long approvedComments;
        private long rejectedComments;
        private long hiddenComments;
        
        // Group Stats
        private long totalGroups;
        private long activeGroups;
        private long frozenGroups;
        private long totalGroupMembers;
        
        // Product Stats
        private long totalProducts;
        private long availableProducts;
    }
}
