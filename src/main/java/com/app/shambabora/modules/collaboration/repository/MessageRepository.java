package com.app.shambabora.modules.collaboration.repository;

import com.app.shambabora.modules.collaboration.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByGroupIdOrderBySentAtDesc(Long groupId, Pageable pageable);
} 