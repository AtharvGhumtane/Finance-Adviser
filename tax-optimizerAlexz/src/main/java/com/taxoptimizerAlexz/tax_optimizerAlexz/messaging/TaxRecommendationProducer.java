package com.taxoptimizerAlexz.tax_optimizerAlexz.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ Producer — publishes TaxRecommendationMessage to the topic exchange.
 *
 * Called by TaxOptimizationService after Gemini returns the AI recommendation.
 * This decouples the DB persistence from the HTTP request lifecycle.
 */
@Component
public class TaxRecommendationProducer {

    private static final Logger log = LoggerFactory.getLogger(TaxRecommendationProducer.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.tax-optimizer}")
    private String exchange;

    @Value("${rabbitmq.routing-key.tax-recommendation}")
    private String routingKey;

    public TaxRecommendationProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publishes the completed tax recommendation to RabbitMQ.
     * The consumer will asynchronously persist it to PostgreSQL.
     *
     * @param message TaxRecommendationMessage containing full result
     */
    public void publishRecommendation(TaxRecommendationMessage message) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            log.info("[RabbitMQ Producer] Published tax recommendation for profileId={}, userId={}",
                    message.getProfileId(), message.getUserId());
        } catch (Exception e) {
            log.error("[RabbitMQ Producer] Failed to publish recommendation for profileId={}: {}",
                    message.getProfileId(), e.getMessage(), e);
            // Non-fatal — recommendation is already returned to user,
            // persistence failure is logged for retry/DLQ processing
        }
    }
}
