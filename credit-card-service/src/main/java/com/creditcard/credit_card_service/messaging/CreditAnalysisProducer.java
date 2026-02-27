package com.creditcard.credit_card_service.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Publishes credit analysis results to RabbitMQ after the HTTP response
 * has been returned to the user. Enables async DB persistence.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreditAnalysisProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.credit-card}")
    private String exchange;

    @Value("${rabbitmq.routing-key.credit-analysis}")
    private String routingKey;

    public void publishAnalysis(CreditAnalysisMessage message) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            log.info("Published credit analysis to RabbitMQ for profile: {} user: {}",
                    message.getProfileId(), message.getUserId());
        } catch (Exception e) {
            log.error("Failed to publish credit analysis message: {}", e.getMessage());
        }
    }
}
