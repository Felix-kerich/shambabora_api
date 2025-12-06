package com.app.shambabora.modules.marketplace.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "mpesa")
public class MpesaConfig {
    private String consumerKey;
    private String consumerSecret;
    private String businessShortCode;
    private String passkey;
    private String callbackUrl;
    private String authUrl;
    private String stkPushUrl;
    private String queryUrl;
    private boolean testMode = false;

    public String getAuthUrl() {
        return authUrl != null ? authUrl : "https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials";
    }

    public String getStkPushUrl() {
        return stkPushUrl != null ? stkPushUrl : "https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest";
    }

    public String getQueryUrl() {
        return queryUrl != null ? queryUrl : "https://sandbox.safaricom.co.ke/mpesa/stkpushquery/v1/query";
    }
}
