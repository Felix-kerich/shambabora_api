package com.app.shambabora.modules.collaboration.controller;

import com.app.shambabora.common.api.ApiResponse;
import com.app.shambabora.common.api.PageResponse;
import com.app.shambabora.modules.collaboration.dto.MessageDTO;
import com.app.shambabora.modules.collaboration.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/collaboration/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<ApiResponse<MessageDTO>> send(@RequestBody @Valid MessageDTO request) {
        return ResponseEntity.ok(ApiResponse.ok("Message sent", messageService.send(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MessageDTO>>> list(
            @RequestParam Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<MessageDTO> messages = messageService.list(groupId, page, size);
        PageResponse<MessageDTO> body = PageResponse.<MessageDTO>builder()
                .content(messages.getContent())
                .page(messages.getNumber())
                .size(messages.getSize())
                .totalElements(messages.getTotalElements())
                .totalPages(messages.getTotalPages())
                .build();
        return ResponseEntity.ok(ApiResponse.ok(body));
    }
} 