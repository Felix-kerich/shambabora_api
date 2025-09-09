package com.app.shambabora.messaging;



import com.app.shambabora.config.RabbitMQConfig;
import com.app.shambabora.dto.DiseaseDetectionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DiseaseDetectionPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishDiseaseDetectionJob(DiseaseDetectionRequest request) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.FARM_ADVISOR_EXCHANGE,
                RabbitMQConfig.DISEASE_DETECTION_ROUTING_KEY,
                request
        );
    }
}
