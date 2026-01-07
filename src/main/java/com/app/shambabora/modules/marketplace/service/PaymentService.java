package com.app.shambabora.modules.marketplace.service;

import com.app.shambabora.common.exception.BadRequestException;
import com.app.shambabora.common.exception.NotFoundException;
import com.app.shambabora.modules.marketplace.config.MpesaConfig;
import com.app.shambabora.modules.marketplace.dto.*;
import com.app.shambabora.modules.marketplace.entity.Order;
import com.app.shambabora.modules.marketplace.entity.Payment;
import com.app.shambabora.modules.marketplace.repository.OrderRepository;
import com.app.shambabora.modules.marketplace.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final MpesaConfig mpesaConfig;
    
    @Qualifier("mpesaRestTemplate")
    private final RestTemplate restTemplate;

    /**
     * Initiate M-Pesa STK Push payment
     */
    @Transactional
    public PaymentInitiationResponse initiatePayment(PaymentInitiationRequest request) {
        // Validate order exists
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order not found"));

        // Check if payment already exists
        paymentRepository.findByOrderId(request.getOrderId()).ifPresent(p -> {
            if ("PENDING".equals(p.getStatus())) {
                throw new BadRequestException("Payment already pending for this order");
            }
        });

        // Validate phone number format (should be 254XXXXXXXXX)
        String phoneNumber = normalizePhoneNumber(request.getPhoneNumber());
        if (!isValidPhoneNumber(phoneNumber)) {
            throw new BadRequestException("Invalid phone number format. Use 254XXXXXXXXX");
        }

        try {
            // Get access token
            String accessToken = getAccessToken();

            // Format amount as whole number (M-Pesa doesn't accept decimals)
            // Convert 250.00 to "250", 100.50 to "101" (round up)
            String amount = String.valueOf(order.getTotalPrice().intValue());

            // Create STK Push request
            MpesaStkPushRequest stkRequest = buildStkPushRequest(
                    amount,
                    phoneNumber,
                    request.getAccountReference()
            );

            // Send STK Push
            MpesaStkPushResponse stkResponse = sendStkPush(accessToken, stkRequest);

            // Save payment record
            Payment payment = new Payment();
            payment.setOrderId(request.getOrderId());
            payment.setAmount(order.getTotalPrice());
            payment.setStatus("PENDING");
            payment.setCheckoutRequestId(stkResponse.getCheckoutRequestId());
            payment.setMerchantRequestId(stkResponse.getMerchantRequestId());
            payment.setPhoneNumber(phoneNumber);
            payment.setResponseDescription(stkResponse.getResponseDescription());

            Payment savedPayment = paymentRepository.save(payment);
            log.info("Payment initiated: paymentId={}, orderId={}, checkoutRequestId={}", 
                    savedPayment.getId(), request.getOrderId(), stkResponse.getCheckoutRequestId());

            // Build response
            PaymentInitiationResponse response = new PaymentInitiationResponse();
            response.setPaymentId(savedPayment.getId());
            response.setOrderId(request.getOrderId());
            response.setCheckoutRequestId(stkResponse.getCheckoutRequestId());
            response.setResponseCode(stkResponse.getResponseCode());
            response.setResponseDescription(stkResponse.getResponseDescription());
            response.setCustomerMessage(stkResponse.getCustomerMessage());

            return response;
        } catch (Exception e) {
            log.error("Error initiating payment for order: {}", request.getOrderId(), e);
            throw new BadRequestException("Failed to initiate payment: " + e.getMessage());
        }
    }

    /**
     * Process M-Pesa callback
     */
    @Transactional
    public void processCallback(PaymentCallbackRequest callbackRequest) {
        try {
            PaymentCallbackRequest.CallbackBody.StkCallback stkCallback = 
                    callbackRequest.getBody().getStkCallback();

            String checkoutRequestId = stkCallback.getCheckoutRequestId();
            int resultCode = stkCallback.getResultCode();

            // Find payment by checkout request ID
            Payment payment = paymentRepository.findByCheckoutRequestId(checkoutRequestId)
                    .orElseThrow(() -> new NotFoundException("Payment not found for checkout: " + checkoutRequestId));

            if (resultCode == 0) {
                // Payment successful
                handleSuccessfulPayment(payment, stkCallback);
            } else {
                // Payment failed
                handleFailedPayment(payment, stkCallback.getResultDesc());
            }
        } catch (Exception e) {
            log.error("Error processing M-Pesa callback", e);
            throw new BadRequestException("Error processing callback: " + e.getMessage());
        }
    }

    /**
     * Get payment status
     */
    public PaymentStatusResponse getPaymentStatus(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found"));

        PaymentStatusResponse response = new PaymentStatusResponse();
        response.setPaymentId(payment.getId());
        response.setOrderId(payment.getOrderId());
        response.setStatus(payment.getStatus());
        response.setAmount(payment.getAmount());
        response.setTransactionCode(payment.getTransactionCode());
        response.setPhoneNumber(payment.getPhoneNumber());
        response.setCreatedAt(payment.getCreatedAt());
        response.setPaidAt(payment.getPaidAt());

        return response;
    }

    /**
     * Get access token from M-Pesa Daraja API
     */
    private String getAccessToken() {
        // Test mode - return mock token
        if (mpesaConfig.isTestMode()) {
            log.info("Test mode enabled - using mock access token");
            return "test_access_token_" + System.currentTimeMillis();
        }
        
        try {
            // Validate credentials are loaded
            String consumerKey = mpesaConfig.getConsumerKey();
            String consumerSecret = mpesaConfig.getConsumerSecret();
            
            if (consumerKey == null || consumerKey.trim().isEmpty() || 
                consumerSecret == null || consumerSecret.trim().isEmpty()) {
                log.error("M-Pesa credentials not configured. Check application.properties");
                log.error("Consumer Key: {}", consumerKey == null ? "NULL" : (consumerKey.isEmpty() ? "EMPTY" : "SET"));
                log.error("Consumer Secret: {}", consumerSecret == null ? "NULL" : (consumerSecret.isEmpty() ? "EMPTY" : "SET"));
                throw new RuntimeException("M-Pesa credentials not configured");
            }
            
            // Log credential details for debugging (without exposing full values)
            log.info("Daraja Sandbox Authentication Details:");
            log.info("  Consumer Key: {}...{} ({})", 
                    consumerKey.substring(0, Math.min(5, consumerKey.length())),
                    consumerKey.substring(Math.max(0, consumerKey.length() - 5)),
                    consumerKey.length());
            log.info("  Consumer Secret: {}...{} ({})", 
                    consumerSecret.substring(0, Math.min(5, consumerSecret.length())),
                    consumerSecret.substring(Math.max(0, consumerSecret.length() - 5)),
                    consumerSecret.length());
            
            // Create Basic Auth header
            String auth = consumerKey + ":" + consumerSecret;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + encodedAuth);

            // For Daraja OAuth, use GET request with Basic Auth (not POST)
            HttpEntity<Void> request = new HttpEntity<>(headers);
            
            String authUrl = mpesaConfig.getAuthUrl();
            log.info("Requesting access token from: {}", authUrl);
            log.info("Authorization header: Basic " + encodedAuth.substring(0, Math.min(20, encodedAuth.length())) + "...");
            
            // USE GET REQUEST, NOT POST
            ResponseEntity<String> response = restTemplate.exchange(
                    authUrl,
                    org.springframework.http.HttpMethod.GET,
                    request,
                    String.class
            );

            log.info("Auth response status: {}", response.getStatusCode());
            String responseBody = response.getBody();
            log.info("Auth response body length: {}", responseBody != null ? responseBody.length() : 0);
            
            if (responseBody != null && responseBody.length() > 0) {
                log.debug("Auth response body: {}", responseBody);
            }
            
            // Check if response is successful and has body
            if (response.getStatusCode().is2xxSuccessful()) {
                if (responseBody != null && !responseBody.trim().isEmpty()) {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        MpesaAuthResponse authResponse = objectMapper.readValue(responseBody, MpesaAuthResponse.class);
                        
                        String accessToken = authResponse.getAccessToken();
                        if (accessToken != null && !accessToken.trim().isEmpty()) {
                            log.info("✓ Access token obtained successfully, expires in: {}", authResponse.getExpiresIn());
                            return accessToken;
                        } else {
                            log.error("✗ Access token is null or empty in parsed response");
                            throw new RuntimeException("Failed to get access token - token is empty");
                        }
                    } catch (com.fasterxml.jackson.core.JsonProcessingException parseException) {
                        log.error("✗ Failed to parse M-Pesa auth response as JSON: {}", responseBody);
                        log.error("Parse error details: {}", parseException.getMessage());
                        throw new RuntimeException("Failed to parse access token response: " + parseException.getMessage());
                    }
                } else {
                    log.error("✗ Auth response body is empty or null");
                    log.error("DARAJA SANDBOX - Common causes of empty response with 200 status:");
                    log.error("  1. INVALID CONSUMER KEY or CONSUMER SECRET");
                    log.error("  2. Consumer Key/Secret mismatch");
                    log.error("  3. App not activated on Daraja portal");
                    log.error("  4. Whitelist IP not configured (add your server IP to Daraja whitelist)");
                    log.error("  5. Invalid Base64 encoding of credentials");
                    log.error("");
                    log.error("TO FIX:");
                    log.error("  - Verify credentials from: https://developer.safaricom.co.ke/");
                    log.error("  - Ensure app is active (check status on Daraja portal)");
                    log.error("  - Add your server IP to Daraja whitelist");
                    log.error("  - Test credentials manually using curl:");
                    log.error("    curl -u 'KEY:SECRET' 'https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials'");
                    throw new RuntimeException("Failed to get access token - empty response body (likely invalid credentials)");
                }
            } else {
                log.error("✗ Auth request failed with status: {}", response.getStatusCode());
                log.error("Response body: {}", responseBody);
                throw new RuntimeException("Failed to get access token - HTTP " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            log.error("✗ M-Pesa auth client error (4xx): {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            log.error("This usually means invalid credentials. Verify consumer key and secret in application.properties");
            throw new RuntimeException("M-Pesa authentication failed (client error): " + e.getStatusCode());
        } catch (HttpServerErrorException e) {
            log.error("✗ M-Pesa auth server error (5xx): {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("M-Pesa authentication failed (server error): " + e.getStatusCode());
        } catch (Exception e) {
            log.error("✗ Error getting M-Pesa access token: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to authenticate with M-Pesa: " + e.getMessage());
        }
    }

    /**
     * Send STK Push to M-Pesa
     */
    private MpesaStkPushResponse sendStkPush(String accessToken, MpesaStkPushRequest request) {
        // Test mode - return mock response
        if (mpesaConfig.isTestMode()) {
            log.info("Test mode enabled - returning mock STK Push response");
            MpesaStkPushResponse mockResponse = new MpesaStkPushResponse();
            mockResponse.setResponseCode("0");
            mockResponse.setResponseDescription("Success. Request accepted for processing");
            mockResponse.setMerchantRequestId("test_merchant_" + System.currentTimeMillis());
            mockResponse.setCheckoutRequestId("test_checkout_" + System.currentTimeMillis());
            mockResponse.setCustomerMessage("Success. Request accepted for processing");
            return mockResponse;
        }
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + accessToken);

            HttpEntity<MpesaStkPushRequest> httpRequest = new HttpEntity<>(request, headers);
            
            String stkPushUrl = mpesaConfig.getStkPushUrl();
            log.info("Sending STK Push to: {}", stkPushUrl);
            log.info("STK Push Request Details:");
            log.info("  Amount: {}", request.getAmount());
            log.info("  Phone: {}", request.getPhoneNumber());
            log.info("  Account Reference: {}", request.getAccountReference());
            log.info("  Business Short Code: {}", request.getBusinessShortCode());
            log.info("  Timestamp: {}", request.getTimestamp());
            log.info("  Password (encoded): {}", request.getPassword());
            log.info("  Party A: {}", request.getPartyA());
            log.info("  Party B: {}", request.getPartyB());
            log.info("  Callback URL: {}", request.getCallBackUrl());
            
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String requestJson = objectMapper.writeValueAsString(request);
                log.debug("STK Push Request JSON: {}", requestJson);
            } catch (Exception e) {
                log.warn("Failed to serialize request for logging: {}", e.getMessage());
            }
            
            ResponseEntity<MpesaStkPushResponse> response = restTemplate.postForEntity(
                    stkPushUrl,
                    httpRequest,
                    MpesaStkPushResponse.class
            );

            log.info("STK Push Response status: {}", response.getStatusCode());
            
            if (response.getBody() != null) {
                log.info("✓ STK Push successful!");
                log.info("  Response Code: {}", response.getBody().getResponseCode());
                log.info("  Response Description: {}", response.getBody().getResponseDescription());
                log.info("  Checkout Request ID: {}", response.getBody().getCheckoutRequestId());
                log.info("  Merchant Request ID: {}", response.getBody().getMerchantRequestId());
                return response.getBody();
            }
            
            log.error("✗ STK Push response body is null");
            log.error("Response status: {}", response.getStatusCode());
            throw new RuntimeException("Failed to send STK Push - empty response body");
            
        } catch (HttpClientErrorException e) {
            log.error("✗ M-Pesa STK Push client error (4xx): {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            log.error("Possible causes:");
            log.error("  1. Invalid password (check BusinessShortCode + PassKey + Timestamp encoding)");
            log.error("  2. Invalid phone number format (should be 254XXXXXXXXX)");
            log.error("  3. Invalid amount format");
            log.error("  4. Invalid callback URL");
            throw new RuntimeException("STK Push failed (client error): " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            log.error("✗ M-Pesa STK Push server error (5xx): {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("STK Push failed (server error): " + e.getStatusCode());
        } catch (Exception e) {
            log.error("✗ Error sending STK Push: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send payment prompt: " + e.getMessage());
        }
    }

    /**
     * Build STK Push request
     */
    private MpesaStkPushRequest buildStkPushRequest(String amount, String phoneNumber, String accountReference) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = now.format(formatter);

        // Build password: Base64(BusinessShortCode + PassKey + Timestamp)
        String passwordString = mpesaConfig.getBusinessShortCode() + mpesaConfig.getPasskey() + timestamp;
        String password = Base64.getEncoder().encodeToString(
                passwordString.getBytes(StandardCharsets.UTF_8)
        );

        log.debug("Password Encoding Details:");
        log.debug("  Business Short Code: {}", mpesaConfig.getBusinessShortCode());
        log.debug("  PassKey length: {}", mpesaConfig.getPasskey() != null ? mpesaConfig.getPasskey().length() : 0);
        log.debug("  Timestamp: {}", timestamp);
        log.debug("  Password String (before encoding): {}", passwordString);
        log.debug("  Password String length: {}", passwordString.length());
        log.debug("  Base64 Encoded Password: {}", password);

        MpesaStkPushRequest request = new MpesaStkPushRequest();
        request.setBusinessShortCode(mpesaConfig.getBusinessShortCode());
        request.setPassword(password);
        request.setTimestamp(timestamp);
        request.setAmount(amount);
        request.setPartyA(phoneNumber);
        request.setPartyB(mpesaConfig.getBusinessShortCode());
        request.setPhoneNumber(phoneNumber);
        request.setCallBackUrl(mpesaConfig.getCallbackUrl());
        request.setAccountReference(accountReference);
        request.setTransactionDesc("Order Payment");

        return request;
    }

    /**
     * Handle successful payment
     */
    @Transactional
    private void handleSuccessfulPayment(Payment payment, PaymentCallbackRequest.CallbackBody.StkCallback stkCallback) {
        // Extract transaction code from callback metadata
        String transactionCode = extractTransactionCode(stkCallback.getCallbackMetadata());

        payment.setStatus("PAID");
        payment.setTransactionCode(transactionCode);
        payment.setPaidAt(Instant.now());
        paymentRepository.save(payment);

        // Update order status to PAID
        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order not found"));
        order.setStatus("PAID");
        orderRepository.save(order);

        // Mark product as sold (set available to false)
        // This is handled by the OrderService when order status changes to PAID

        log.info("Payment successful: paymentId={}, orderId={}, transactionCode={}", 
                payment.getId(), payment.getOrderId(), transactionCode);
    }

    /**
     * Handle failed payment
     */
    @Transactional
    private void handleFailedPayment(Payment payment, String resultDescription) {
        payment.setStatus("FAILED");
        payment.setResponseDescription(resultDescription);
        paymentRepository.save(payment);

        log.warn("Payment failed: paymentId={}, orderId={}, reason={}", 
                payment.getId(), payment.getOrderId(), resultDescription);
    }

    /**
     * Extract transaction code from callback metadata
     */
    private String extractTransactionCode(PaymentCallbackRequest.CallbackBody.StkCallback.CallbackMetadata metadata) {
        if (metadata != null && metadata.getItems() != null) {
            for (PaymentCallbackRequest.CallbackBody.StkCallback.CallbackMetadata.Item item : metadata.getItems()) {
                if ("MpesaReceiptNumber".equals(item.getName())) {
                    return item.getValue().toString();
                }
            }
        }
        return null;
    }

    /**
     * Normalize phone number to 254XXXXXXXXX format
     */
    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        phoneNumber = phoneNumber.replaceAll("[^0-9]", "");
        if (phoneNumber.startsWith("0")) {
            phoneNumber = "254" + phoneNumber.substring(1);
        } else if (!phoneNumber.startsWith("254")) {
            phoneNumber = "254" + phoneNumber;
        }
        return phoneNumber;
    }

    /**
     * Validate phone number format
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^254[0-9]{9}$");
    }
}
