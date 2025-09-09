package com.app.shambabora.modules.collaboration.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "author_id", nullable = false)
    private Long authorId;
    
    @Column(name = "group_id")
    private Long groupId; // null for general feed posts
    
    @Column(nullable = false, length = 2000)
    private String content;
    
    @Column(length = 500)
    private String imageUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostType postType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus status = PostStatus.ACTIVE;
    
    @Column(name = "moderated_by")
    private Long moderatedBy; // admin/extension officer who moderated
    
    @Column(name = "moderation_notes", length = 500)
    private String moderationNotes;
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PostLike> likes = new ArrayList<>();
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PostComment> comments = new ArrayList<>();
    
    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    private Instant updatedAt;
    
    public enum PostType {
        GENERAL,
        QUESTION,
        ADVICE,
        SHARE_EXPERIENCE,
        MARKET_UPDATE,
        WEATHER_ALERT,
        ANNOUNCEMENT
    }
    
    public enum PostStatus {
        ACTIVE,
        PENDING_MODERATION,
        APPROVED,
        REJECTED,
        HIDDEN
    }
}
