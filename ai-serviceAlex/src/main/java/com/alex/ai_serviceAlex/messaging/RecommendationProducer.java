package com.alex.ai_serviceAlex.messaging;

import com.alex.ai_serviceAlex.dto.InvestmentProfileDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.recommendation}")
    private String exchangeName;

    @Value("${rabbitmq.routing-key.recommendation-request}")
    private String routingKey;

    /**
     * Send recommendation request to RabbitMQ queue
     */
    public void sendRecommendationRequest(InvestmentProfileDTO profile) {
        try {
            log.info("Publishing to RabbitMQ - User: {}, Crypto: {}",
                    profile.getUserId(), profile.getTargetCryptocurrency());

            rabbitTemplate.convertAndSend(exchangeName, routingKey, profile);

            log.info("✅ Message published to queue for user: {}", profile.getUserId());

        } catch (Exception e) {
            log.error("❌ Failed to publish message for user: {}", profile.getUserId(), e);
            throw e;
        }
    }
}