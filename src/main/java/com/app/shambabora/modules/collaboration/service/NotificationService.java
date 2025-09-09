package com.app.shambabora.modules.collaboration.service;

import com.app.shambabora.modules.collaboration.dto.DirectMessageDTO;
import com.app.shambabora.modules.collaboration.dto.PostDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Profile("ws")
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    public void notifyNewPost(PostDTO post) {
        log.info("Notifying about new post: {}", post.getId());
        
        if (post.getGroupId() != null) {
            messagingTemplate.convertAndSend("/topic/group." + post.getGroupId(), post);
        } else {
            messagingTemplate.convertAndSend("/topic/feed", post);
        }
    }
    
    public void notifyNewMessage(DirectMessageDTO message) {
        log.info("Notifying about new message: {}", message.getId());
        
        messagingTemplate.convertAndSendToUser(
                message.getRecipientId().toString(),
                "/queue/messages",
                message
        );
    }
    
    public void notifyPostLike(Long postId, Long userId, String action) {
        log.info("Notifying about post like: post={}, user={}, action={}", postId, userId, action);
        
        LikeNotification notification = new LikeNotification(postId, userId, action);
        messagingTemplate.convertAndSend("/topic/post." + postId, notification);
    }
    
    public void notifyPostComment(Long postId, Long userId, String content) {
        log.info("Notifying about post comment: post={}, user={}", postId, userId);
        
        CommentNotification notification = new CommentNotification(postId, userId, content);
        messagingTemplate.convertAndSend("/topic/post." + postId, notification);
    }
    
    public void notifyGroupUpdate(Long groupId, String updateType, Object data) {
        log.info("Notifying about group update: group={}, type={}", groupId, updateType);
        
        GroupUpdateNotification notification = new GroupUpdateNotification(groupId, updateType, data);
        messagingTemplate.convertAndSend("/topic/group." + groupId, notification);
    }
    
    public void notifyModerationUpdate(Long postId, String status, String notes) {
        log.info("Notifying about moderation update: post={}, status={}", postId, status);
        
        ModerationNotification notification = new ModerationNotification(postId, status, notes);
        messagingTemplate.convertAndSend("/topic/post." + postId, notification);
    }
    
    public static class LikeNotification {
        private Long postId;
        private Long userId;
        private String action;
        private long timestamp;
        
        public LikeNotification(Long postId, Long userId, String action) {
            this.postId = postId;
            this.userId = userId;
            this.action = action;
            this.timestamp = System.currentTimeMillis();
        }
        
        public Long getPostId() { return postId; }
        public Long getUserId() { return userId; }
        public String getAction() { return action; }
        public long getTimestamp() { return timestamp; }
    }
    
    public static class CommentNotification {
        private Long postId;
        private Long userId;
        private String content;
        private long timestamp;
        
        public CommentNotification(Long postId, Long userId, String content) {
            this.postId = postId;
            this.userId = userId;
            this.content = content;
            this.timestamp = System.currentTimeMillis();
        }
        
        public Long getPostId() { return postId; }
        public Long getUserId() { return userId; }
        public String getContent() { return content; }
        public long getTimestamp() { return timestamp; }
    }
    
    public static class GroupUpdateNotification {
        private Long groupId;
        private String updateType;
        private Object data;
        private long timestamp;
        
        public GroupUpdateNotification(Long groupId, String updateType, Object data) {
            this.groupId = groupId;
            this.updateType = updateType;
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
        
        public Long getGroupId() { return groupId; }
        public String getUpdateType() { return updateType; }
        public Object getData() { return data; }
        public long getTimestamp() { return timestamp; }
    }
    
    public static class ModerationNotification {
        private Long postId;
        private String status;
        private String notes;
        private long timestamp;
        
        public ModerationNotification(Long postId, String status, String notes) {
            this.postId = postId;
            this.status = status;
            this.notes = notes;
            this.timestamp = System.currentTimeMillis();
        }
        
        public Long getPostId() { return postId; }
        public String getStatus() { return status; }
        public String getNotes() { return notes; }
        public long getTimestamp() { return timestamp; }
    }
}
