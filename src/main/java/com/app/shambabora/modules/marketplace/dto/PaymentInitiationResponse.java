package com.app.shambabora.modules.marketplace.dto;

import lombok.Data;

@Data
public class PaymentInitiationResponse {
    private Long paymentId;
    private Long orderId;
    private String checkoutRequestId;
    private String responseCode;
    private String responseDescription;
    private String customerMessage;
}
