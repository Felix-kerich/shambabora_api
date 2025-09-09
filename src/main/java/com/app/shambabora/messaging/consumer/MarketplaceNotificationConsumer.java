package com.app.shambabora.messaging.consumer;


//import lombok.extern.slf4j.Slf4j;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.stereotype.Component;
//
//import static com.farmadvisor.config.RabbitMQConfig.MARKETPLACE_NOTIFICATION_QUEUE;
//
//@Component
//@Slf4j
//public class MarketplaceNotificationConsumer {
//
//    @RabbitListener(queues = MARKETPLACE_NOTIFICATION_QUEUE)
//    public void handleMarketplaceNotification(MarketplaceNotification notification) {
//        log.info("Received marketplace notification: {}", notification);
//
//        // Here you would implement the actual notification delivery logic
//        // This could be sending an email, SMS, or push notification
//
//        log.info("Notification sent to user {}: {}", notification.getUserId(), notification.getMessage());
//    }
//}
