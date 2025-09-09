package com.app.shambabora.modules.collaboration.repository;

import com.app.shambabora.modules.collaboration.entity.DirectMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {
    
    // Get conversation between two users
    @Query("SELECT dm FROM DirectMessage dm WHERE " +
           "((dm.senderId = :userId1 AND dm.recipientId = :userId2) OR " +
           "(dm.senderId = :userId2 AND dm.recipientId = :userId1)) " +
           "ORDER BY dm.createdAt ASC")
    Page<DirectMessage> findConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2, Pageable pageable);
    
    // Get recent conversations for a user
    @Query("SELECT dm FROM DirectMessage dm WHERE dm.id IN " +
           "(SELECT MAX(dm2.id) FROM DirectMessage dm2 WHERE " +
           "(dm2.senderId = :userId OR dm2.recipientId = :userId) " +
           "GROUP BY CASE WHEN dm2.senderId = :userId THEN dm2.recipientId ELSE dm2.senderId END) " +
           "ORDER BY dm.createdAt DESC")
    Page<DirectMessage> findRecentConversations(@Param("userId") Long userId, Pageable pageable);
    
    // Get unread messages for a user
    @Query("SELECT dm FROM DirectMessage dm WHERE dm.recipientId = :userId AND dm.readAt IS NULL ORDER BY dm.createdAt ASC")
    List<DirectMessage> findUnreadMessages(@Param("userId") Long userId);
    
    // Count unread messages for a user
    long countByRecipientIdAndReadAtIsNull(Long userId);
    
    // Get messages after a specific timestamp (for real-time updates)
    @Query("SELECT dm FROM DirectMessage dm WHERE " +
           "((dm.senderId = :userId1 AND dm.recipientId = :userId2) OR " +
           "(dm.senderId = :userId2 AND dm.recipientId = :userId1)) " +
           "AND dm.createdAt > :since ORDER BY dm.createdAt ASC")
    List<DirectMessage> findMessagesAfter(@Param("userId1") Long userId1, @Param("userId2") Long userId2, @Param("since") Instant since);
    
    // Get all users who have conversations with a user
    @Query("SELECT DISTINCT CASE WHEN dm.senderId = :userId THEN dm.recipientId ELSE dm.senderId END " +
           "FROM DirectMessage dm WHERE dm.senderId = :userId OR dm.recipientId = :userId")
    List<Long> findConversationPartners(@Param("userId") Long userId);
}
