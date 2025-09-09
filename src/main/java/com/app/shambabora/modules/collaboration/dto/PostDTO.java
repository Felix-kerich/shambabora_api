package com.app.shambabora.modules.collaboration.dto;

import com.app.shambabora.modules.collaboration.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {
    private Long id;
    private Long authorId;
    private String authorName;
    private Long groupId;
    private String groupName;
    private String content;
    private String imageUrl;
    private Post.PostType postType;
    private Post.PostStatus status;
    private Long moderatedBy;
    private String moderationNotes;
    private int likeCount;
    private int commentCount;
    private boolean isLikedByCurrentUser;
    private List<PostCommentDTO> recentComments;
    private Instant createdAt;
    private Instant updatedAt;
}
