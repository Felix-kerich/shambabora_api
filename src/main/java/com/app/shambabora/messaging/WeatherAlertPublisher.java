package com.app.shambabora.messaging;


import com.app.shambabora.config.RabbitMQConfig;
import com.app.shambabora.dto.WeatherAlertRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WeatherAlertPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishWeatherAlertJob(WeatherAlertRequest request) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.FARM_ADVISOR_EXCHANGE,
                RabbitMQConfig.WEATHER_ALERT_ROUTING_KEY,
                request
        );
    }
}
