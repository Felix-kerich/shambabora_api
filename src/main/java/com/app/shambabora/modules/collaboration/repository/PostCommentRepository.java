package com.app.shambabora.modules.collaboration.repository;

import com.app.shambabora.modules.collaboration.entity.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    
    // Changed to remove hardcoded sorting - now uses Pageable's sort parameter
    Page<PostComment> findByPostIdAndStatus(Long postId, PostComment.CommentStatus status, Pageable pageable);
    
    List<PostComment> findByPostIdAndParentCommentIdIsNullAndStatusOrderByCreatedAtAsc(Long postId, PostComment.CommentStatus status);
    
    List<PostComment> findByParentCommentIdAndStatusOrderByCreatedAtAsc(Long parentCommentId, PostComment.CommentStatus status);
    
    long countByPostIdAndStatus(Long postId, PostComment.CommentStatus status);
    
    long countByStatus(PostComment.CommentStatus status);
    
    Page<PostComment> findByStatusOrderByCreatedAtDesc(PostComment.CommentStatus status, Pageable pageable);
    
    @Query("SELECT pc FROM PostComment pc WHERE pc.content LIKE %:keyword% AND pc.status = :status ORDER BY pc.createdAt DESC")
    Page<PostComment> findByContentContainingAndStatus(@Param("keyword") String keyword, @Param("status") PostComment.CommentStatus status, Pageable pageable);
}
