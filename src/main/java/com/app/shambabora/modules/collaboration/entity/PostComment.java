package com.app.shambabora.modules.collaboration.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "post_comments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostComment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
    
    @Column(name = "author_id", nullable = false)
    private Long authorId;
    
    @Column(nullable = false, length = 1000)
    private String content;
    
    @Column(name = "parent_comment_id")
    private Long parentCommentId; // for nested replies
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommentStatus status = CommentStatus.ACTIVE;
    
    @Column(name = "moderated_by")
    private Long moderatedBy;
    
    @Column(name = "moderation_notes", length = 500)
    private String moderationNotes;
    
    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    private Instant updatedAt;
    
    public enum CommentStatus {
        ACTIVE,
        PENDING_MODERATION,
        APPROVED,
        REJECTED,
        HIDDEN
    }
}
