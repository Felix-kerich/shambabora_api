package com.app.shambabora.modules.marketplace.dto;

import lombok.Data;

@Data
public class PaymentInitiationRequest {
    private Long orderId;
    private String phoneNumber;
    private String accountReference;
}
