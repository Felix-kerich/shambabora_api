package com.app.shambabora.modules.marketplace.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class PaymentStatusResponse {
    private Long paymentId;
    private Long orderId;
    private String status;
    private BigDecimal amount;
    private String transactionCode;
    private String phoneNumber;
    private Instant createdAt;
    private Instant paidAt;
}
