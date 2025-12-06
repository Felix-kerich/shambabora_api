package com.app.shambabora.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Application-wide configuration for beans and settings.
 */
@Configuration
public class ApplicationConfig {

    /**
     * Configure RestTemplate with timeout settings for external service calls.
     * AI analysis can take up to 60 seconds, so we set a 2-minute timeout.
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5 seconds
        factory.setReadTimeout(240000); // 120 seconds (2 minutes) - Allow time for AI analysis
        
        ClientHttpRequestFactory bufferingFactory = new BufferingClientHttpRequestFactory(factory);
        return new RestTemplate(bufferingFactory);
    }
}
