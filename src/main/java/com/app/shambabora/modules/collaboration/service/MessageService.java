package com.app.shambabora.modules.collaboration.service;

import com.app.shambabora.common.exception.BadRequestException;
import com.app.shambabora.common.exception.NotFoundException;
import com.app.shambabora.modules.collaboration.dto.MessageDTO;
import com.app.shambabora.modules.collaboration.entity.Message;
import com.app.shambabora.modules.collaboration.repository.GroupRepository;
import com.app.shambabora.modules.collaboration.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final GroupRepository groupRepository;

    @Transactional
    public MessageDTO send(MessageDTO request) {
        if (request.getGroupId() == null || request.getSenderId() == null) {
            throw new BadRequestException("groupId and senderId are required");
        }
        groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new NotFoundException("Group not found"));
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new BadRequestException("content is required");
        }
        Message message = new Message();
        message.setGroupId(request.getGroupId());
        message.setSenderId(request.getSenderId());
        message.setContent(request.getContent());
        Message saved = messageRepository.save(message);
        log.info("Message sent id={} groupId={}", saved.getId(), saved.getGroupId());
        return toDto(saved);
    }

    public Page<MessageDTO> list(Long groupId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return messageRepository.findByGroupIdOrderBySentAtDesc(groupId, pageable)
                .map(this::toDto);
    }

    private MessageDTO toDto(Message entity) {
        MessageDTO dto = new MessageDTO();
        dto.setId(entity.getId());
        dto.setGroupId(entity.getGroupId());
        dto.setSenderId(entity.getSenderId());
        dto.setContent(entity.getContent());
        dto.setSentAt(entity.getSentAt());
        return dto;
    }
} 