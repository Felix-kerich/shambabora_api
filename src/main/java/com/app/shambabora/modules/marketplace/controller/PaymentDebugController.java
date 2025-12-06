package com.app.shambabora.modules.marketplace.controller;

import com.app.shambabora.common.api.ApiResponse;
import com.app.shambabora.modules.marketplace.config.MpesaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/marketplace/debug")
@RequiredArgsConstructor
public class PaymentDebugController {

    private final MpesaConfig mpesaConfig;

    /**
     * Debug endpoint to verify M-Pesa configuration
     */
    @GetMapping("/mpesa-config")
    public ResponseEntity<ApiResponse<Object>> getMpesaConfig() {
        return ResponseEntity.ok(ApiResponse.ok("M-Pesa Configuration", new Object() {
            public String authUrl = mpesaConfig.getAuthUrl();
            public String stkPushUrl = mpesaConfig.getStkPushUrl();
            public String queryUrl = mpesaConfig.getQueryUrl();
            public String callbackUrl = mpesaConfig.getCallbackUrl();
            public String businessShortCode = mpesaConfig.getBusinessShortCode();
            public boolean hasConsumerKey = mpesaConfig.getConsumerKey() != null && !mpesaConfig.getConsumerKey().isEmpty();
            public boolean hasConsumerSecret = mpesaConfig.getConsumerSecret() != null && !mpesaConfig.getConsumerSecret().isEmpty();
            public boolean hasPasskey = mpesaConfig.getPasskey() != null && !mpesaConfig.getPasskey().isEmpty();
        }));
    }
}
