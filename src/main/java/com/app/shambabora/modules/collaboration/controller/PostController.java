package com.app.shambabora.modules.collaboration.controller;

import com.app.shambabora.common.api.ApiResponse;
import com.app.shambabora.common.api.PageResponse;
import com.app.shambabora.modules.collaboration.dto.PostCommentDTO;
import com.app.shambabora.modules.collaboration.dto.PostDTO;
import com.app.shambabora.modules.collaboration.entity.Post;
import com.app.shambabora.modules.collaboration.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/collaboration/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {
    
    private final PostService postService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<PostDTO>> createPost(@Valid @RequestBody PostDTO postDTO, 
                                                          @RequestHeader("X-User-Id") Long userId) {
        log.info("Creating post for user: {}", userId);
        return ResponseEntity.ok(postService.createPost(postDTO, userId));
    }
    
    @GetMapping("/feed")
    public ResponseEntity<ApiResponse<PageResponse<PostDTO>>> getFeed(@RequestHeader("X-User-Id") Long userId,
                                                                     @PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting feed for user: {}", userId);
        return ResponseEntity.ok(postService.getFeed(userId, pageable));
    }
    
    @GetMapping("/group/{groupId}")
    public ResponseEntity<ApiResponse<PageResponse<PostDTO>>> getGroupPosts(@PathVariable Long groupId,
                                                                           @RequestHeader("X-User-Id") Long userId,
                                                                           @PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting posts for group: {}", groupId);
        return ResponseEntity.ok(postService.getGroupPosts(groupId, userId, pageable));
    }
    
    @PostMapping("/group/{groupId}")
    public ResponseEntity<ApiResponse<PostDTO>> createPostForGroup(@PathVariable Long groupId,
                                                                   @Valid @RequestBody PostDTO postDTO,
                                                                   @RequestHeader("X-User-Id") Long userId) {
        log.info("Creating post for group: {} by user: {}", groupId, userId);
        // Set the groupId from the path variable to ensure it matches
        postDTO.setGroupId(groupId);
        return ResponseEntity.ok(postService.createPost(postDTO, userId));
    }
    
    @PostMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<PostDTO>> likePost(@PathVariable Long postId,
                                                        @RequestHeader("X-User-Id") Long userId) {
        log.info("Liking post: {} by user: {}", postId, userId);
        return ResponseEntity.ok(postService.likePost(postId, userId));
    }
    
    @DeleteMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<PostDTO>> unlikePost(@PathVariable Long postId,
                                                          @RequestHeader("X-User-Id") Long userId) {
        log.info("Unliking post: {} by user: {}", postId, userId);
        return ResponseEntity.ok(postService.unlikePost(postId, userId));
    }
    
    @PostMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<PostCommentDTO>> addComment(@PathVariable Long postId,
                                                                @Valid @RequestBody PostCommentDTO commentDTO,
                                                                @RequestHeader("X-User-Id") Long userId) {
        log.info("Adding comment to post: {} by user: {}", postId, userId);
        commentDTO.setPostId(postId);
        return ResponseEntity.ok(postService.addComment(commentDTO, userId));
    }
    
    @GetMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<PageResponse<PostCommentDTO>>> getPostComments(@PathVariable Long postId,
                                                                                    @PageableDefault(size = 10) Pageable pageable) {
        log.info("Getting comments for post: {}", postId);
        return ResponseEntity.ok(postService.getPostComments(postId, pageable));
    }
    
    // Admin/Moderator endpoints
    @PostMapping("/{postId}/moderate")
    public ResponseEntity<ApiResponse<PostDTO>> moderatePost(@PathVariable Long postId,
                                                            @RequestParam Post.PostStatus status,
                                                            @RequestParam(required = false) String notes,
                                                            @RequestHeader("X-User-Id") Long moderatorId) {
        log.info("Moderating post: {} to status: {} by moderator: {}", postId, status, moderatorId);
        return ResponseEntity.ok(postService.moderatePost(postId, status, moderatorId, notes));
    }
    
    @GetMapping("/pending-moderation")
    public ResponseEntity<ApiResponse<PageResponse<PostDTO>>> getPostsPendingModeration(@PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting posts pending moderation");
        return ResponseEntity.ok(postService.getPostsPendingModeration(pageable));
    }

    // New: Flag a post for admin review (does not hide it)
    @PostMapping("/{postId}/flag")
    public ResponseEntity<ApiResponse<Void>> flagPost(@PathVariable Long postId,
                                                      @RequestParam(required = false) String reason,
                                                      @RequestHeader("X-User-Id") Long userId) {
        log.info("User {} flagging post {}. Reason: {}", userId, postId, reason);
        return ResponseEntity.ok(postService.flagPost(postId, userId, reason));
    }
}
