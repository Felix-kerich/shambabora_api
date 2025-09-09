package com.app.shambabora.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Queue names
    public static final String DISEASE_DETECTION_QUEUE = "disease-detection-queue";
    public static final String WEATHER_ALERT_QUEUE = "weather-alert-queue";
    public static final String MARKETPLACE_NOTIFICATION_QUEUE = "marketplace-notification-queue";

    // Exchange names
    public static final String FARM_ADVISOR_EXCHANGE = "farm-advisor-exchange";

    // Routing keys
    public static final String DISEASE_DETECTION_ROUTING_KEY = "disease.detection";
    public static final String WEATHER_ALERT_ROUTING_KEY = "weather.alert";
    public static final String MARKETPLACE_NOTIFICATION_ROUTING_KEY = "marketplace.notification";

    @Bean
    public Queue diseaseDetectionQueue() {
        return new Queue(DISEASE_DETECTION_QUEUE, true);
    }

    @Bean
    public Queue weatherAlertQueue() {
        return new Queue(WEATHER_ALERT_QUEUE, true);
    }

    @Bean
    public Queue marketplaceNotificationQueue() {
        return new Queue(MARKETPLACE_NOTIFICATION_QUEUE, true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(FARM_ADVISOR_EXCHANGE);
    }

    @Bean
    public Binding diseaseDetectionBinding(Queue diseaseDetectionQueue, TopicExchange exchange) {
        return BindingBuilder.bind(diseaseDetectionQueue).to(exchange).with(DISEASE_DETECTION_ROUTING_KEY);
    }

    @Bean
    public Binding weatherAlertBinding(Queue weatherAlertQueue, TopicExchange exchange) {
        return BindingBuilder.bind(weatherAlertQueue).to(exchange).with(WEATHER_ALERT_ROUTING_KEY);
    }

    @Bean
    public Binding marketplaceNotificationBinding(Queue marketplaceNotificationQueue, TopicExchange exchange) {
        return BindingBuilder.bind(marketplaceNotificationQueue).to(exchange).with(MARKETPLACE_NOTIFICATION_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
