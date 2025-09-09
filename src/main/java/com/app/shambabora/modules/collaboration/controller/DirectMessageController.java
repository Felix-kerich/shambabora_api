package com.app.shambabora.modules.collaboration.controller;

import com.app.shambabora.common.api.ApiResponse;
import com.app.shambabora.common.api.PageResponse;
import com.app.shambabora.modules.collaboration.dto.DirectMessageDTO;
import com.app.shambabora.modules.collaboration.service.DirectMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/collaboration/direct-messages")
@RequiredArgsConstructor
@Slf4j
public class DirectMessageController {
    
    private final DirectMessageService directMessageService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<DirectMessageDTO>> sendMessage(@Valid @RequestBody DirectMessageDTO messageDTO,
                                                                    @RequestHeader("X-User-Id") Long senderId) {
        log.info("Sending message from {} to {}", senderId, messageDTO.getRecipientId());
        return ResponseEntity.ok(directMessageService.sendMessage(messageDTO, senderId));
    }
    
    @GetMapping("/conversation/{otherUserId}")
    public ResponseEntity<ApiResponse<PageResponse<DirectMessageDTO>>> getConversation(@PathVariable Long otherUserId,
                                                                                      @RequestHeader("X-User-Id") Long currentUserId,
                                                                                      @PageableDefault(size = 50) Pageable pageable) {
        log.info("Getting conversation between {} and {}", currentUserId, otherUserId);
        return ResponseEntity.ok(directMessageService.getConversation(currentUserId, otherUserId, pageable));
    }
    
    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<PageResponse<DirectMessageDTO>>> getRecentConversations(@RequestHeader("X-User-Id") Long userId,
                                                                                             @PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting recent conversations for user: {}", userId);
        return ResponseEntity.ok(directMessageService.getRecentConversations(userId, pageable));
    }
    
    @PostMapping("/read/{messageId}")
    public ResponseEntity<ApiResponse<DirectMessageDTO>> markAsRead(@PathVariable Long messageId,
                                                                     @RequestHeader("X-User-Id") Long userId) {
        log.info("Marking message {} as read by user {}", messageId, userId);
        return ResponseEntity.ok(directMessageService.markAsRead(messageId, userId));
    }
    
    @GetMapping("/conversation/{otherUserId}/after")
    public ResponseEntity<ApiResponse<List<DirectMessageDTO>>> getMessagesAfter(@PathVariable Long otherUserId,
                                                                               @RequestParam String since,
                                                                               @RequestHeader("X-User-Id") Long currentUserId) {
        log.info("Getting messages after {} between {} and {}", since, currentUserId, otherUserId);
        Instant sinceInstant = Instant.parse(since);
        return ResponseEntity.ok(directMessageService.getMessagesAfter(currentUserId, otherUserId, sinceInstant));
    }
    
    @GetMapping("/partners")
    public ResponseEntity<ApiResponse<List<Long>>> getConversationPartners(@RequestHeader("X-User-Id") Long userId) {
        log.info("Getting conversation partners for user: {}", userId);
        return ResponseEntity.ok(directMessageService.getConversationPartners(userId));
    }
}
