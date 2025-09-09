package com.app.shambabora.modules.collaboration.service;

import com.app.shambabora.common.api.ApiResponse;
import com.app.shambabora.common.api.PageResponse;
import com.app.shambabora.common.exception.BadRequestException;
import com.app.shambabora.common.exception.NotFoundException;
import com.app.shambabora.modules.collaboration.dto.PostCommentDTO;
import com.app.shambabora.modules.collaboration.dto.PostDTO;
import com.app.shambabora.modules.collaboration.entity.Post;
import com.app.shambabora.modules.collaboration.entity.PostComment;
import com.app.shambabora.modules.collaboration.entity.PostLike;
import com.app.shambabora.modules.collaboration.repository.PostCommentRepository;
import com.app.shambabora.modules.collaboration.repository.PostLikeRepository;
import com.app.shambabora.modules.collaboration.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostCommentRepository postCommentRepository;
    private final Optional<NotificationService> notificationService;
    
    @Transactional
    public ApiResponse<PostDTO> createPost(PostDTO postDTO, Long authorId) {
        log.info("Creating post for author: {}", authorId);
        
        Post post = Post.builder()
                .authorId(authorId)
                .groupId(postDTO.getGroupId())
                .content(postDTO.getContent())
                .imageUrl(postDTO.getImageUrl())
                .postType(postDTO.getPostType())
                .status(Post.PostStatus.PENDING_MODERATION)
                .build();
        
        Post savedPost = postRepository.save(post);
        log.info("Post created with ID: {}", savedPost.getId());
        
        // Notify about new post (if websockets enabled)
        notificationService.ifPresent(ns -> ns.notifyNewPost(mapToDTO(savedPost, authorId)));
        
        return ApiResponse.ok("Post created successfully", mapToDTO(savedPost, authorId));
    }
    
    public ApiResponse<PageResponse<PostDTO>> getFeed(Long userId, Pageable pageable) {
        log.info("Getting feed for user: {}", userId);
        
        // Get user's groups
        List<Long> userGroupIds = getUserGroupIds(userId);
        
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        
        Page<Post> posts = postRepository.findByGroupIdInOrGroupIdIsNullAndStatus(
                userGroupIds, Post.PostStatus.ACTIVE, sortedPageable);
        
        List<PostDTO> postDTOs = posts.getContent().stream()
                .map(post -> mapToDTO(post, userId))
                .collect(Collectors.toList());
        
        PageResponse<PostDTO> pageResponse = PageResponse.<PostDTO>builder()
                .content(postDTOs)
                .page(posts.getNumber())
                .size(posts.getSize())
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .build();
        
        return ApiResponse.ok("Feed retrieved successfully", pageResponse);
    }
    
    public ApiResponse<PageResponse<PostDTO>> getGroupPosts(Long groupId, Long userId, Pageable pageable) {
        log.info("Getting posts for group: {} by user: {}", groupId, userId);
        
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        
        Page<Post> posts = postRepository.findByGroupIdAndStatusOrderByCreatedAtDesc(
                groupId, Post.PostStatus.ACTIVE, sortedPageable);
        
        List<PostDTO> postDTOs = posts.getContent().stream()
                .map(post -> mapToDTO(post, userId))
                .collect(Collectors.toList());
        
        PageResponse<PostDTO> pageResponse = PageResponse.<PostDTO>builder()
                .content(postDTOs)
                .page(posts.getNumber())
                .size(posts.getSize())
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .build();
        
        return ApiResponse.ok("Group posts retrieved successfully", pageResponse);
    }
    
    @Transactional
    public ApiResponse<PostDTO> likePost(Long postId, Long userId) {
        log.info("User {} liking post {}", userId, postId);
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        
        if (postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new BadRequestException("Post already liked by user");
        }
        
        PostLike like = PostLike.builder()
                .post(post)
                .userId(userId)
                .build();
        
        postLikeRepository.save(like);
        log.info("Post {} liked by user {}", postId, userId);
        
        // Notify about like (if websockets enabled)
        notificationService.ifPresent(ns -> ns.notifyPostLike(postId, userId, "like"));
        
        return ApiResponse.ok("Post liked successfully", mapToDTO(post, userId));
    }
    
    @Transactional
    public ApiResponse<PostDTO> unlikePost(Long postId, Long userId) {
        log.info("User {} unliking post {}", userId, postId);
        
        PostLike like = postLikeRepository.findByPostIdAndUserId(postId, userId)
                .orElseThrow(() -> new NotFoundException("Like not found"));
        
        postLikeRepository.delete(like);
        log.info("Post {} unliked by user {}", postId, userId);
        
        // Notify about unlike (if websockets enabled)
        notificationService.ifPresent(ns -> ns.notifyPostLike(postId, userId, "unlike"));
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        
        return ApiResponse.ok("Post unliked successfully", mapToDTO(post, userId));
    }
    
    @Transactional
    public ApiResponse<PostCommentDTO> addComment(PostCommentDTO commentDTO, Long authorId) {
        log.info("Adding comment to post {} by user {}", commentDTO.getPostId(), authorId);
        
        Post post = postRepository.findById(commentDTO.getPostId())
                .orElseThrow(() -> new NotFoundException("Post not found"));
        
        PostComment comment = PostComment.builder()
                .post(post)
                .authorId(authorId)
                .content(commentDTO.getContent())
                .parentCommentId(commentDTO.getParentCommentId())
                .status(PostComment.CommentStatus.PENDING_MODERATION)
                .build();
        
        PostComment savedComment = postCommentRepository.save(comment);
        log.info("Comment created with ID: {}", savedComment.getId());
        
        // Notify about comment (if websockets enabled)
        notificationService.ifPresent(ns -> ns.notifyPostComment(commentDTO.getPostId(), authorId, commentDTO.getContent()));
        
        return ApiResponse.ok("Comment added successfully", mapCommentToDTO(savedComment));
    }
    
    public ApiResponse<PageResponse<PostCommentDTO>> getPostComments(Long postId, Pageable pageable) {
        log.info("Getting comments for post: {}", postId);
        
        Page<PostComment> comments = postCommentRepository.findByPostIdAndStatusOrderByCreatedAtAsc(
                postId, PostComment.CommentStatus.ACTIVE, pageable);
        
        List<PostCommentDTO> commentDTOs = comments.getContent().stream()
                .map(this::mapCommentToDTO)
                .collect(Collectors.toList());
        
        PageResponse<PostCommentDTO> pageResponse = PageResponse.<PostCommentDTO>builder()
                .content(commentDTOs)
                .page(comments.getNumber())
                .size(comments.getSize())
                .totalElements(comments.getTotalElements())
                .totalPages(comments.getTotalPages())
                .build();
        
        return ApiResponse.ok("Comments retrieved successfully", pageResponse);
    }
    
    // Admin/Moderator methods
    @Transactional
    public ApiResponse<PostDTO> moderatePost(Long postId, Post.PostStatus status, Long moderatorId, String notes) {
        log.info("Moderating post {} to status {} by moderator {}", postId, status, moderatorId);
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        
        post.setStatus(status);
        post.setModeratedBy(moderatorId);
        post.setModerationNotes(notes);
        
        Post savedPost = postRepository.save(post);
        log.info("Post {} moderated to status {}", postId, status);
        
        return ApiResponse.ok("Post moderated successfully", mapToDTO(savedPost, moderatorId));
    }
    
    public ApiResponse<PageResponse<PostDTO>> getPostsPendingModeration(Pageable pageable) {
        log.info("Getting posts pending moderation");
        
        Page<Post> posts = postRepository.findByStatusOrderByCreatedAtDesc(
                Post.PostStatus.PENDING_MODERATION, pageable);
        
        List<PostDTO> postDTOs = posts.getContent().stream()
                .map(post -> mapToDTO(post, null))
                .collect(Collectors.toList());
        
        PageResponse<PostDTO> pageResponse = PageResponse.<PostDTO>builder()
                .content(postDTOs)
                .page(posts.getNumber())
                .size(posts.getSize())
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .build();
        
        return ApiResponse.ok("Posts pending moderation retrieved successfully", pageResponse);
    }
    
    private PostDTO mapToDTO(Post post, Long currentUserId) {
        int likeCount = (int) postLikeRepository.countByPostId(post.getId());
        int commentCount = (int) postCommentRepository.countByPostIdAndStatus(post.getId(), PostComment.CommentStatus.ACTIVE);
        boolean isLiked = currentUserId != null && postLikeRepository.existsByPostIdAndUserId(post.getId(), currentUserId);
        
        return PostDTO.builder()
                .id(post.getId())
                .authorId(post.getAuthorId())
                .authorName(getUserName(post.getAuthorId()))
                .groupId(post.getGroupId())
                .groupName(getGroupName(post.getGroupId()))
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .postType(post.getPostType())
                .status(post.getStatus())
                .moderatedBy(post.getModeratedBy())
                .moderationNotes(post.getModerationNotes())
                .likeCount(likeCount)
                .commentCount(commentCount)
                .isLikedByCurrentUser(isLiked)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
    
    private PostCommentDTO mapCommentToDTO(PostComment comment) {
        return PostCommentDTO.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .authorId(comment.getAuthorId())
                .authorName(getUserName(comment.getAuthorId()))
                .content(comment.getContent())
                .parentCommentId(comment.getParentCommentId())
                .status(comment.getStatus())
                .moderatedBy(comment.getModeratedBy())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
    
    private List<Long> getUserGroupIds(Long userId) {
        return List.of();
    }
    
    private String getUserName(Long userId) {
        return "User " + userId;
    }
    
    private String getGroupName(Long groupId) {
        if (groupId == null) return null;
        return "Group " + groupId;
    }
    
    public ApiResponse<PageResponse<PostDTO>> getPostsByStatus(Post.PostStatus status, Pageable pageable) {
        log.info("Getting posts with status: {}", status);
        
        Page<Post> posts = postRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        
        List<PostDTO> postDTOs = posts.getContent().stream()
                .map(post -> mapToDTO(post, null))
                .collect(Collectors.toList());
        
        PageResponse<PostDTO> pageResponse = PageResponse.<PostDTO>builder()
                .content(postDTOs)
                .page(posts.getNumber())
                .size(posts.getSize())
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .build();
        
        return ApiResponse.ok("Posts retrieved successfully", pageResponse);
    }
    
    @Transactional
    public ApiResponse<PostCommentDTO> moderateComment(Long commentId, PostComment.CommentStatus status, Long moderatorId, String notes) {
        log.info("Moderating comment {} to status {} by moderator {}", commentId, status, moderatorId);
        
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));
        
        comment.setStatus(status);
        comment.setModeratedBy(moderatorId);
        
        PostComment savedComment = postCommentRepository.save(comment);
        log.info("Comment {} moderated to status {}", commentId, status);
        
        return ApiResponse.ok("Comment moderated successfully", mapCommentToDTO(savedComment));
    }
    
    public ApiResponse<PageResponse<PostCommentDTO>> getCommentsPendingModeration(Pageable pageable) {
        log.info("Getting comments pending moderation");
        
        Page<PostComment> comments = postCommentRepository.findByStatusOrderByCreatedAtDesc(
                PostComment.CommentStatus.PENDING_MODERATION, pageable);
        
        List<PostCommentDTO> commentDTOs = comments.getContent().stream()
                .map(this::mapCommentToDTO)
                .collect(Collectors.toList());
        
        PageResponse<PostCommentDTO> pageResponse = PageResponse.<PostCommentDTO>builder()
                .content(commentDTOs)
                .page(comments.getNumber())
                .size(comments.getSize())
                .totalElements(comments.getTotalElements())
                .totalPages(comments.getTotalPages())
                .build();
        
        return ApiResponse.ok("Comments pending moderation retrieved successfully", pageResponse);
    }
    
    public ApiResponse<AdminStatsDTO> getAdminStats() {
        log.info("Getting admin statistics");
        
        AdminStatsDTO stats = new AdminStatsDTO();
        stats.setTotalPosts(postRepository.count());
        stats.setPendingPosts(postRepository.countByStatus(Post.PostStatus.PENDING_MODERATION));
        stats.setApprovedPosts(postRepository.countByStatus(Post.PostStatus.APPROVED));
        stats.setRejectedPosts(postRepository.countByStatus(Post.PostStatus.REJECTED));
        stats.setHiddenPosts(postRepository.countByStatus(Post.PostStatus.HIDDEN));
        stats.setTotalComments(postCommentRepository.count());
        stats.setPendingComments(postCommentRepository.countByStatus(PostComment.CommentStatus.PENDING_MODERATION));
        stats.setApprovedComments(postCommentRepository.countByStatus(PostComment.CommentStatus.APPROVED));
        stats.setRejectedComments(postCommentRepository.countByStatus(PostComment.CommentStatus.REJECTED));
        stats.setHiddenComments(postCommentRepository.countByStatus(PostComment.CommentStatus.HIDDEN));
        
        return ApiResponse.ok("Admin statistics retrieved successfully", stats);
    }
    
    public static class AdminStatsDTO {
        private long totalPosts;
        private long pendingPosts;
        private long approvedPosts;
        private long rejectedPosts;
        private long hiddenPosts;
        private long totalComments;
        private long pendingComments;
        private long approvedComments;
        private long rejectedComments;
        private long hiddenComments;
        
        public long getTotalPosts() { return totalPosts; }
        public void setTotalPosts(long totalPosts) { this.totalPosts = totalPosts; }
        public long getPendingPosts() { return pendingPosts; }
        public void setPendingPosts(long pendingPosts) { this.pendingPosts = pendingPosts; }
        public long getApprovedPosts() { return approvedPosts; }
        public void setApprovedPosts(long approvedPosts) { this.approvedPosts = approvedPosts; }
        public long getRejectedPosts() { return rejectedPosts; }
        public void setRejectedPosts(long rejectedPosts) { this.rejectedPosts = rejectedPosts; }
        public long getHiddenPosts() { return hiddenPosts; }
        public void setHiddenPosts(long hiddenPosts) { this.hiddenPosts = hiddenPosts; }
        public long getTotalComments() { return totalComments; }
        public void setTotalComments(long totalComments) { this.totalComments = totalComments; }
        public long getPendingComments() { return pendingComments; }
        public void setPendingComments(long pendingComments) { this.pendingComments = pendingComments; }
        public long getApprovedComments() { return approvedComments; }
        public void setApprovedComments(long approvedComments) { this.approvedComments = approvedComments; }
        public long getRejectedComments() { return rejectedComments; }
        public void setRejectedComments(long rejectedComments) { this.rejectedComments = rejectedComments; }
        public long getHiddenComments() { return hiddenComments; }
        public void setHiddenComments(long hiddenComments) { this.hiddenComments = hiddenComments; }
    }
}
