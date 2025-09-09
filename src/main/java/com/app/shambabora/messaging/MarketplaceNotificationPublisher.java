package com.app.shambabora.messaging;

import com.app.shambabora.config.RabbitMQConfig;
import com.app.shambabora.dto.MarketplaceNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MarketplaceNotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishMarketplaceNotification(MarketplaceNotification notification) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.FARM_ADVISOR_EXCHANGE,
                RabbitMQConfig.MARKETPLACE_NOTIFICATION_ROUTING_KEY,
                notification
        );
    }
}
