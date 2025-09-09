package com.app.shambabora.modules.collaboration.service;

import com.app.shambabora.common.api.ApiResponse;
import com.app.shambabora.common.api.PageResponse;
import com.app.shambabora.common.exception.BadRequestException;
import com.app.shambabora.common.exception.NotFoundException;
import com.app.shambabora.modules.collaboration.dto.DirectMessageDTO;
import com.app.shambabora.modules.collaboration.entity.DirectMessage;
import com.app.shambabora.modules.collaboration.repository.DirectMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DirectMessageService {
    
    private final DirectMessageRepository directMessageRepository;
    private final Optional<NotificationService> notificationService;
    
    @Transactional
    public ApiResponse<DirectMessageDTO> sendMessage(DirectMessageDTO messageDTO, Long senderId) {
        log.info("Sending message from {} to {}", senderId, messageDTO.getRecipientId());
        
        if (senderId.equals(messageDTO.getRecipientId())) {
            throw new BadRequestException("Cannot send message to yourself");
        }
        
        DirectMessage message = DirectMessage.builder()
                .senderId(senderId)
                .recipientId(messageDTO.getRecipientId())
                .content(messageDTO.getContent())
                .imageUrl(messageDTO.getImageUrl())
                .messageType(messageDTO.getMessageType())
                .status(DirectMessage.MessageStatus.SENT)
                .build();
        
        DirectMessage savedMessage = directMessageRepository.save(message);
        log.info("Message sent with ID: {}", savedMessage.getId());
        
        // Notify recipient if websockets enabled
        notificationService.ifPresent(ns -> ns.notifyNewMessage(mapToDTO(savedMessage)));
        
        return ApiResponse.ok("Message sent successfully", mapToDTO(savedMessage));
    }
    
    public ApiResponse<PageResponse<DirectMessageDTO>> getConversation(Long userId1, Long userId2, Pageable pageable) {
        log.info("Getting conversation between {} and {}", userId1, userId2);
        
        Page<DirectMessage> messages = directMessageRepository.findConversation(userId1, userId2, pageable);
        
        List<DirectMessageDTO> messageDTOs = messages.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        PageResponse<DirectMessageDTO> pageResponse = PageResponse.<DirectMessageDTO>builder()
                .content(messageDTOs)
                .page(messages.getNumber())
                .size(messages.getSize())
                .totalElements(messages.getTotalElements())
                .totalPages(messages.getTotalPages())
                .build();
        
        return ApiResponse.ok("Conversation retrieved successfully", pageResponse);
    }
    
    public ApiResponse<PageResponse<DirectMessageDTO>> getRecentConversations(Long userId, Pageable pageable) {
        log.info("Getting recent conversations for user: {}", userId);
        
        Page<DirectMessage> messages = directMessageRepository.findRecentConversations(userId, pageable);
        
        List<DirectMessageDTO> messageDTOs = messages.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        PageResponse<DirectMessageDTO> pageResponse = PageResponse.<DirectMessageDTO>builder()
                .content(messageDTOs)
                .page(messages.getNumber())
                .size(messages.getSize())
                .totalElements(messages.getTotalElements())
                .totalPages(messages.getTotalPages())
                .build();
        
        return ApiResponse.ok("Recent conversations retrieved successfully", pageResponse);
    }
    
    @Transactional
    public ApiResponse<DirectMessageDTO> markAsRead(Long messageId, Long userId) {
        log.info("Marking message {} as read by user {}", messageId, userId);
        
        DirectMessage message = directMessageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Message not found"));
        
        if (!message.getRecipientId().equals(userId)) {
            throw new BadRequestException("Cannot mark message as read - not the recipient");
        }
        
        message.setReadAt(Instant.now());
        message.setStatus(DirectMessage.MessageStatus.READ);
        
        DirectMessage savedMessage = directMessageRepository.save(message);
        log.info("Message {} marked as read", messageId);
        
        return ApiResponse.ok("Message marked as read", mapToDTO(savedMessage));
    }
    
    @Transactional
    public ApiResponse<String> markConversationAsRead(Long otherUserId, Long currentUserId) {
        log.info("Marking conversation with {} as read by user {}", otherUserId, currentUserId);
        
        List<DirectMessage> unreadMessages = directMessageRepository.findUnreadMessages(currentUserId);
        
        unreadMessages.stream()
                .filter(msg -> msg.getSenderId().equals(otherUserId))
                .forEach(msg -> {
                    msg.setReadAt(Instant.now());
                    msg.setStatus(DirectMessage.MessageStatus.READ);
                });
        
        directMessageRepository.saveAll(unreadMessages);
        log.info("Conversation with {} marked as read", otherUserId);
        
        return ApiResponse.ok("Conversation marked as read", "Success");
    }
    
    public ApiResponse<List<DirectMessageDTO>> getUnreadMessages(Long userId) {
        log.info("Getting unread messages for user: {}", userId);
        
        List<DirectMessage> unreadMessages = directMessageRepository.findUnreadMessages(userId);
        
        List<DirectMessageDTO> messageDTOs = unreadMessages.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        return ApiResponse.ok("Unread messages retrieved successfully", messageDTOs);
    }
    
    public ApiResponse<Long> getUnreadCount(Long userId) {
        log.info("Getting unread message count for user: {}", userId);
        
        long count = directMessageRepository.countByRecipientIdAndReadAtIsNull(userId);
        
        return ApiResponse.ok("Unread count retrieved successfully", count);
    }
    
    public ApiResponse<List<DirectMessageDTO>> getMessagesAfter(Long userId1, Long userId2, Instant since) {
        log.info("Getting messages after {} between {} and {}", since, userId1, userId2);
        
        List<DirectMessage> messages = directMessageRepository.findMessagesAfter(userId1, userId2, since);
        
        List<DirectMessageDTO> messageDTOs = messages.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        return ApiResponse.ok("Messages retrieved successfully", messageDTOs);
    }
    
    public ApiResponse<List<Long>> getConversationPartners(Long userId) {
        log.info("Getting conversation partners for user: {}", userId);
        
        List<Long> partners = directMessageRepository.findConversationPartners(userId);
        
        return ApiResponse.ok("Conversation partners retrieved successfully", partners);
    }
    
    private DirectMessageDTO mapToDTO(DirectMessage message) {
        return DirectMessageDTO.builder()
                .id(message.getId())
                .senderId(message.getSenderId())
                .senderName(getUserName(message.getSenderId()))
                .recipientId(message.getRecipientId())
                .recipientName(getUserName(message.getRecipientId()))
                .content(message.getContent())
                .imageUrl(message.getImageUrl())
                .messageType(message.getMessageType())
                .status(message.getStatus())
                .readAt(message.getReadAt())
                .createdAt(message.getCreatedAt())
                .build();
    }
    
    private String getUserName(Long userId) {
        return "User " + userId;
    }
}
