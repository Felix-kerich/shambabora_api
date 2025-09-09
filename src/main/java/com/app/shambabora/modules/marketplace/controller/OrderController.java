package com.app.shambabora.modules.marketplace.controller;

import com.app.shambabora.common.api.ApiResponse;
import com.app.shambabora.common.api.PageResponse;
import com.app.shambabora.modules.marketplace.dto.OrderDTO;
import com.app.shambabora.modules.marketplace.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/marketplace/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDTO>> place(@RequestBody @Valid OrderDTO request) {
        return ResponseEntity.ok(ApiResponse.ok("Order placed", orderService.place(request)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderDTO>> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated", orderService.updateStatus(id, status)));
    }

    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<ApiResponse<PageResponse<OrderDTO>>> byBuyer(
            @PathVariable Long buyerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<OrderDTO> orders = orderService.listByBuyer(buyerId, page, size);
        PageResponse<OrderDTO> body = PageResponse.<OrderDTO>builder()
                .content(orders.getContent())
                .page(orders.getNumber())
                .size(orders.getSize())
                .totalElements(orders.getTotalElements())
                .totalPages(orders.getTotalPages())
                .build();
        return ResponseEntity.ok(ApiResponse.ok(body));
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<ApiResponse<PageResponse<OrderDTO>>> bySeller(
            @PathVariable Long sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<OrderDTO> orders = orderService.listBySeller(sellerId, page, size);
        PageResponse<OrderDTO> body = PageResponse.<OrderDTO>builder()
                .content(orders.getContent())
                .page(orders.getNumber())
                .size(orders.getSize())
                .totalElements(orders.getTotalElements())
                .totalPages(orders.getTotalPages())
                .build();
        return ResponseEntity.ok(ApiResponse.ok(body));
    }
} 