package com.app.shambabora.modules.marketplace.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    /**
     * M-Pesa specific RestTemplate with buffering for payment operations.
     * This RestTemplate is configured with BufferingClientHttpRequestFactory
     * to ensure response bodies are properly buffered and can be read multiple times.
     * 
     * Uses a different bean name to avoid conflicts with the main RestTemplate in ApplicationConfig.
     */
    @Bean(name = "mpesaRestTemplate")
    public RestTemplate mpesaRestTemplate(RestTemplateBuilder builder) {
        return builder
                .requestFactory(this::clientHttpRequestFactory)
                .interceptors((request, body, execution) -> {
                    // Log request details for debugging
                    return execution.execute(request, body);
                })
                .build();
    }

    /**
     * Creates a ClientHttpRequestFactory with proper buffering.
     * The BufferingClientHttpRequestFactory ensures that the response body
     * can be read multiple times, which is necessary for RestTemplate to properly
     * deserialize responses.
     */
    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000); // 15 seconds
        factory.setReadTimeout(40000);    // 40 seconds
        return new BufferingClientHttpRequestFactory(factory);
    }
}
