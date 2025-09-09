package com.app.shambabora.modules.collaboration.controller;

import com.app.shambabora.modules.collaboration.dto.DirectMessageDTO;
import com.app.shambabora.modules.collaboration.dto.PostDTO;
import com.app.shambabora.modules.collaboration.service.DirectMessageService;
import com.app.shambabora.modules.collaboration.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@Profile("ws")
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final DirectMessageService directMessageService;
    private final PostService postService;
    
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload DirectMessageDTO messageDTO) {
        log.info("WebSocket: Sending message from {} to {}", messageDTO.getSenderId(), messageDTO.getRecipientId());
        
        messagingTemplate.convertAndSendToUser(
                messageDTO.getRecipientId().toString(),
                "/queue/messages",
                messageDTO
        );
        
        messagingTemplate.convertAndSendToUser(
                messageDTO.getSenderId().toString(),
                "/queue/messages",
                messageDTO
        );
    }
    
    @MessageMapping("/chat.addUser")
    public void addUser(@Payload String username) {
        log.info("WebSocket: User {} joined", username);
        
        messagingTemplate.convertAndSend("/topic/public", username + " joined the chat");
    }
    
    @MessageMapping("/post.like")
    public void likePost(@Payload PostLikeRequest request) {
        log.info("WebSocket: User {} liking post {}", request.getUserId(), request.getPostId());
        
        messagingTemplate.convertAndSend("/topic/post." + request.getPostId(), request);
    }
    
    @MessageMapping("/post.comment")
    public void commentOnPost(@Payload PostCommentRequest request) {
        log.info("WebSocket: User {} commenting on post {}", request.getUserId(), request.getPostId());
        
        messagingTemplate.convertAndSend("/topic/post." + request.getPostId(), request);
    }
    
    @MessageMapping("/group.{groupId}")
    public void sendGroupMessage(@DestinationVariable Long groupId, @Payload GroupMessageRequest request) {
        log.info("WebSocket: Sending message to group {}", groupId);
        
        messagingTemplate.convertAndSend("/topic/group." + groupId, request);
    }
    
    public static class PostLikeRequest {
        private Long postId;
        private Long userId;
        private String action;
        
        public Long getPostId() { return postId; }
        public void setPostId(Long postId) { this.postId = postId; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
    }
    
    public static class PostCommentRequest {
        private Long postId;
        private Long userId;
        private String content;
        
        public Long getPostId() { return postId; }
        public void setPostId(Long postId) { this.postId = postId; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
    
    public static class GroupMessageRequest {
        private Long groupId;
        private Long userId;
        private String content;
        private String messageType;
        
        public Long getGroupId() { return groupId; }
        public void setGroupId(Long groupId) { this.groupId = groupId; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getMessageType() { return messageType; }
        public void setMessageType(String messageType) { this.messageType = messageType; }
    }
}
