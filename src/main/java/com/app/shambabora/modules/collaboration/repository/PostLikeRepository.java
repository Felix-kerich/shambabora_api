package com.app.shambabora.modules.collaboration.repository;

import com.app.shambabora.modules.collaboration.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    
    Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);
    
    boolean existsByPostIdAndUserId(Long postId, Long userId);
    
    long countByPostId(Long postId);
    
    @Query("SELECT pl.userId FROM PostLike pl WHERE pl.post.id = :postId")
    List<Long> findUserIdsByPostId(@Param("postId") Long postId);
    
    void deleteByPostIdAndUserId(Long postId, Long userId);
}
