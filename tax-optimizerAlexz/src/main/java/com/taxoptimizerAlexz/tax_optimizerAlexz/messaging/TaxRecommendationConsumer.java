package com.taxoptimizerAlexz.tax_optimizerAlexz.messaging;

import com.taxoptimizerAlexz.tax_optimizerAlexz.model.TaxRecommendation;
import com.taxoptimizerAlexz.tax_optimizerAlexz.repo.TaxRecommendationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * RabbitMQ Consumer — listens on the tax.recommendation.queue.
 *
 * Receives TaxRecommendationMessage and persists it to the
 * 'tax_recommendations' table using reactive R2DBC.
 *
 * This runs asynchronously, completely outside the HTTP request lifecycle.
 * The user already received their response — this is background persistence.
 */
@Component
public class TaxRecommendationConsumer {

    private static final Logger log = LoggerFactory.getLogger(TaxRecommendationConsumer.class);

    private final TaxRecommendationRepository recommendationRepository;

    public TaxRecommendationConsumer(TaxRecommendationRepository recommendationRepository) {
        this.recommendationRepository = recommendationRepository;
    }

    /**
     * Listens to the tax recommendation queue.
     * Converts the message to a TaxRecommendation entity and saves it via R2DBC.
     *
     * @param message Deserialized TaxRecommendationMessage from RabbitMQ
     */
    @RabbitListener(queues = "${rabbitmq.queue.tax-recommendation}")
    public void consumeRecommendation(TaxRecommendationMessage message) {
        log.info("[RabbitMQ Consumer] Received recommendation for profileId={}, userId={}",
                message.getProfileId(), message.getUserId());

        TaxRecommendation recommendation = TaxRecommendation.builder()
                .profileId(message.getProfileId())
                .userId(message.getUserId())
                .recommendedRegime(message.getRecommendedRegime())
                .oldRegimeTax(message.getOldRegimeTax())
                .newRegimeTax(message.getNewRegimeTax())
                .taxSavingsRegime(message.getTaxSavingsRegime())
                .potentialSavings(message.getPotentialSavings())
                .aiRecommendation(message.getAiRecommendation())
                .strategiesJson(message.getStrategiesJson())
                .financialYear(message.getFinancialYear())
                .status("GENERATED")
                .createdAt(LocalDateTime.now())
                .build();

        // Reactive save — subscribe to trigger execution
        recommendationRepository.save(recommendation)
                .doOnSuccess(saved ->
                        log.info("[RabbitMQ Consumer] Saved recommendation id={} for profileId={}",
                                saved.getId(), saved.getProfileId()))
                .doOnError(error ->
                        log.error("[RabbitMQ Consumer] Failed to save recommendation for profileId={}: {}",
                                message.getProfileId(), error.getMessage(), error))
                .subscribe(); // Non-blocking — fire and forget with logging
    }
}
