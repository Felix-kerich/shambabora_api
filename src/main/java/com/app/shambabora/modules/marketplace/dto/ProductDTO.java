package com.app.shambabora.modules.marketplace.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String unit;
    private int quantity;
    private boolean available;
    private Long sellerId;
    private Instant createdAt;
    private Instant updatedAt;
} 