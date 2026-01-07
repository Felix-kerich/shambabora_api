package com.app.shambabora.modules.collaboration.dto;

import com.app.shambabora.modules.collaboration.entity.PostComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostCommentDTO {
    private Long id;
    private Long postId;
    private Long authorId;
    private String authorName;
    private String content;
    private Long parentCommentId;
    private PostComment.CommentStatus status;
    private Long moderatedBy;
    private String moderationNotes;
    private Instant createdAt;
    private Instant updatedAt;
}
