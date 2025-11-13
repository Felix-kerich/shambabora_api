package com.app.shambabora.modules.collaboration.repository;

import com.app.shambabora.modules.collaboration.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    // Get posts for general feed (not group-specific)
    Page<Post> findByGroupIdIsNullAndStatusOrderByCreatedAtDesc(Post.PostStatus status, Pageable pageable);
    
    // Get posts for a specific group
    Page<Post> findByGroupIdAndStatusOrderByCreatedAtDesc(Long groupId, Post.PostStatus status, Pageable pageable);
    
    // Get posts by author
    Page<Post> findByAuthorIdAndStatusOrderByCreatedAtDesc(Long authorId, Post.PostStatus status, Pageable pageable);
    
    // Get posts pending moderation
    Page<Post> findByStatusOrderByCreatedAtDesc(Post.PostStatus status, Pageable pageable);
    
    // Search posts by content
    @Query("SELECT p FROM Post p WHERE p.content LIKE %:keyword% AND p.status = :status ORDER BY p.createdAt DESC")
    Page<Post> findByContentContainingAndStatus(@Param("keyword") String keyword, @Param("status") Post.PostStatus status, Pageable pageable);
    
    // Get posts by type
    Page<Post> findByPostTypeAndStatusOrderByCreatedAtDesc(Post.PostType postType, Post.PostStatus status, Pageable pageable);
    
    // Get posts for user's groups (only ACTIVE previously)
    @Query("SELECT p FROM Post p WHERE (p.groupId IN :groupIds OR p.groupId IS NULL) AND p.status = :status ORDER BY p.createdAt DESC")
    Page<Post> findByGroupIdInOrGroupIdIsNullAndStatus(@Param("groupIds") List<Long> groupIds, @Param("status") Post.PostStatus status, Pageable pageable);

    // New: Get all visible posts for user's groups (exclude HIDDEN and REJECTED)
    @Query("SELECT p FROM Post p WHERE (p.groupId IN :groupIds OR p.groupId IS NULL) AND p.status <> com.app.shambabora.modules.collaboration.entity.Post$PostStatus.HIDDEN AND p.status <> com.app.shambabora.modules.collaboration.entity.Post$PostStatus.REJECTED ORDER BY p.createdAt DESC")
    Page<Post> findFeedAllVisible(@Param("groupIds") List<Long> groupIds, Pageable pageable);

    // New: Get all visible posts for a group (exclude HIDDEN and REJECTED)
    @Query("SELECT p FROM Post p WHERE p.groupId = :groupId AND p.status <> com.app.shambabora.modules.collaboration.entity.Post$PostStatus.HIDDEN AND p.status <> com.app.shambabora.modules.collaboration.entity.Post$PostStatus.REJECTED ORDER BY p.createdAt DESC")
    Page<Post> findGroupAllVisible(@Param("groupId") Long groupId, Pageable pageable);
    
    // Count posts by status
    long countByStatus(Post.PostStatus status);
    
    // Get trending posts (most liked in last 7 days)
    @Query("SELECT p FROM Post p WHERE p.status = :status AND p.createdAt >= :since ORDER BY SIZE(p.likes) DESC")
    Page<Post> findTrendingPosts(@Param("status") Post.PostStatus status, @Param("since") java.time.Instant since, Pageable pageable);
}
