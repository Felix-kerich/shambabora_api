package com.app.shambabora.modules.marketplace.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class OrderDTO {
    private Long id;
    private Long buyerId;
    private Long sellerId;
    private Long productId;
    private int quantity;
    private BigDecimal totalPrice;
    private String status; // PLACED, PAID, SHIPPED, COMPLETED, CANCELLED
    private Instant createdAt;
} 