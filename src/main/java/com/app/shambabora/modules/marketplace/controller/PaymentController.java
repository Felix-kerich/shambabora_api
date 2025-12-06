package com.app.shambabora.modules.marketplace.controller;

import com.app.shambabora.common.api.ApiResponse;
import com.app.shambabora.modules.marketplace.dto.*;
import com.app.shambabora.modules.marketplace.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/marketplace/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Initiate M-Pesa payment
     * 
     * Request Body:
     * {
     *   "orderId": 1,
     *   "phoneNumber": "254712345678",
     *   "accountReference": "ORDER-001"
     * }
     * 
     * Response:
     * {
     *   "success": true,
     *   "message": "Payment initiated",
     *   "data": {
     *     "paymentId": 1,
     *     "orderId": 1,
     *     "checkoutRequestId": "ws_CO_DMZ_123456789",
     *     "responseCode": "0",
     *     "responseDescription": "Success. Request accepted for processing",
     *     "customerMessage": "Success. Request accepted for processing"
     *   }
     * }
     */
    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<PaymentInitiationResponse>> initiatePayment(
            @RequestBody @Valid PaymentInitiationRequest request) {
        log.info("Initiating payment for order: {}", request.getOrderId());
        PaymentInitiationResponse response = paymentService.initiatePayment(request);
        return ResponseEntity.ok(ApiResponse.ok("Payment initiated", response));
    }

    /**
     * M-Pesa callback endpoint (called by M-Pesa servers)
     * 
     * Request Body (from M-Pesa):
     * {
     *   "Body": {
     *     "stkCallback": {
     *       "MerchantRequestID": "16813-1590513-1",
     *       "CheckoutRequestID": "ws_CO_DMZ_123456789",
     *       "ResultCode": 0,
     *       "ResultDesc": "The service request has been processed successfully.",
     *       "CallbackMetadata": {
     *         "Item": [
     *           {"Name": "Amount", "Value": 1},
     *           {"Name": "MpesaReceiptNumber", "Value": "NLJ7RT61SV"},
     *           {"Name": "TransactionDate", "Value": 20191122063845},
     *           {"Name": "PhoneNumber", "Value": 254712345678}
     *         ]
     *       }
     *     }
     *   }
     * }
     * 
     * Response:
     * {
     *   "ResultCode": 0,
     *   "ResultDesc": "Callback processed successfully"
     * }
     */
    @PostMapping("/callback")
    public ResponseEntity<ApiResponse<Void>> handleCallback(
            @RequestBody PaymentCallbackRequest callbackRequest) {
        log.info("Received M-Pesa callback");
        paymentService.processCallback(callbackRequest);
        return ResponseEntity.ok(ApiResponse.ok("Callback processed", null));
    }

    /**
     * Get payment status
     * 
     * Response:
     * {
     *   "success": true,
     *   "data": {
     *     "paymentId": 1,
     *     "orderId": 1,
     *     "status": "PAID",
     *     "amount": 1000.00,
     *     "transactionCode": "NLJ7RT61SV",
     *     "phoneNumber": "254712345678",
     *     "createdAt": "2024-01-15T10:30:00Z",
     *     "paidAt": "2024-01-15T10:35:00Z"
     *   }
     * }
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> getPaymentStatus(
            @PathVariable Long paymentId) {
        log.info("Getting payment status for paymentId: {}", paymentId);
        PaymentStatusResponse response = paymentService.getPaymentStatus(paymentId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
