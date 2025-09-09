package com.app.shambabora.modules.collaboration.controller;

import com.app.shambabora.common.api.ApiResponse;
import com.app.shambabora.common.api.PageResponse;
import com.app.shambabora.modules.collaboration.dto.PostCommentDTO;
import com.app.shambabora.modules.collaboration.dto.PostDTO;
import com.app.shambabora.modules.collaboration.entity.Post;
import com.app.shambabora.modules.collaboration.entity.PostComment;
import com.app.shambabora.modules.collaboration.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("collaborationAdminController")
@RequestMapping("/api/admin/collaboration")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    
    private final PostService postService;
    
    @GetMapping("/posts/pending")
    public ResponseEntity<ApiResponse<PageResponse<PostDTO>>> getPostsPendingModeration(@PageableDefault(size = 20) Pageable pageable) {
        log.info("Admin getting posts pending moderation");
        return ResponseEntity.ok(postService.getPostsPendingModeration(pageable));
    }
    
    @PostMapping("/posts/{postId}/approve")
    public ResponseEntity<ApiResponse<PostDTO>> approvePost(@PathVariable Long postId,
                                                           @RequestParam(required = false) String notes,
                                                           @RequestHeader("X-User-Id") Long moderatorId) {
        log.info("Admin {} approving post {}", moderatorId, postId);
        return ResponseEntity.ok(postService.moderatePost(postId, Post.PostStatus.APPROVED, moderatorId, notes));
    }
    
    @PostMapping("/posts/{postId}/reject")
    public ResponseEntity<ApiResponse<PostDTO>> rejectPost(@PathVariable Long postId,
                                                          @RequestParam(required = false) String notes,
                                                          @RequestHeader("X-User-Id") Long moderatorId) {
        log.info("Admin {} rejecting post {}", moderatorId, postId);
        return ResponseEntity.ok(postService.moderatePost(postId, Post.PostStatus.REJECTED, moderatorId, notes));
    }
    
    @PostMapping("/posts/{postId}/hide")
    public ResponseEntity<ApiResponse<PostDTO>> hidePost(@PathVariable Long postId,
                                                        @RequestParam(required = false) String notes,
                                                        @RequestHeader("X-User-Id") Long moderatorId) {
        log.info("Admin {} hiding post {}", moderatorId, postId);
        return ResponseEntity.ok(postService.moderatePost(postId, Post.PostStatus.HIDDEN, moderatorId, notes));
    }
    
    @GetMapping("/posts/rejected")
    public ResponseEntity<ApiResponse<PageResponse<PostDTO>>> getRejectedPosts(@PageableDefault(size = 20) Pageable pageable) {
        log.info("Admin getting rejected posts");
        return ResponseEntity.ok(postService.getPostsByStatus(Post.PostStatus.REJECTED, pageable));
    }
    
    @GetMapping("/posts/hidden")
    public ResponseEntity<ApiResponse<PageResponse<PostDTO>>> getHiddenPosts(@PageableDefault(size = 20) Pageable pageable) {
        log.info("Admin getting hidden posts");
        return ResponseEntity.ok(postService.getPostsByStatus(Post.PostStatus.HIDDEN, pageable));
    }
    
    @GetMapping("/posts/approved")
    public ResponseEntity<ApiResponse<PageResponse<PostDTO>>> getApprovedPosts(@PageableDefault(size = 20) Pageable pageable) {
        log.info("Admin getting approved posts");
        return ResponseEntity.ok(postService.getPostsByStatus(Post.PostStatus.APPROVED, pageable));
    }
    
    @PostMapping("/comments/{commentId}/approve")
    public ResponseEntity<ApiResponse<PostCommentDTO>> approveComment(@PathVariable Long commentId,
                                                                     @RequestParam(required = false) String notes,
                                                                     @RequestHeader("X-User-Id") Long moderatorId) {
        log.info("Admin {} approving comment {}", moderatorId, commentId);
        return ResponseEntity.ok(postService.moderateComment(commentId, PostComment.CommentStatus.APPROVED, moderatorId, notes));
    }
    
    @PostMapping("/comments/{commentId}/reject")
    public ResponseEntity<ApiResponse<PostCommentDTO>> rejectComment(@PathVariable Long commentId,
                                                                    @RequestParam(required = false) String notes,
                                                                    @RequestHeader("X-User-Id") Long moderatorId) {
        log.info("Admin {} rejecting comment {}", moderatorId, commentId);
        return ResponseEntity.ok(postService.moderateComment(commentId, PostComment.CommentStatus.REJECTED, moderatorId, notes));
    }
    
    @PostMapping("/comments/{commentId}/hide")
    public ResponseEntity<ApiResponse<PostCommentDTO>> hideComment(@PathVariable Long commentId,
                                                                  @RequestParam(required = false) String notes,
                                                                  @RequestHeader("X-User-Id") Long moderatorId) {
        log.info("Admin {} hiding comment {}", moderatorId, commentId);
        return ResponseEntity.ok(postService.moderateComment(commentId, PostComment.CommentStatus.HIDDEN, moderatorId, notes));
    }
    
    @GetMapping("/comments/pending")
    public ResponseEntity<ApiResponse<PageResponse<PostCommentDTO>>> getCommentsPendingModeration(@PageableDefault(size = 20) Pageable pageable) {
        log.info("Admin getting comments pending moderation");
        return ResponseEntity.ok(postService.getCommentsPendingModeration(pageable));
    }
    
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<PostService.AdminStatsDTO>> getAdminStats() {
        log.info("Admin getting statistics");
        return ResponseEntity.ok(postService.getAdminStats());
    }
    
}
