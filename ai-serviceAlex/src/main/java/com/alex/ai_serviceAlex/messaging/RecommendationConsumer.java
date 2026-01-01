package com.alex.ai_serviceAlex.messaging;

import com.alex.ai_serviceAlex.dto.InvestmentProfileDTO;
import com.alex.ai_serviceAlex.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.annotation.RabbitListener;


@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationConsumer {

    private final RecommendationService recommendationService;

    /**
     * Listen for recommendation request messages
     */
    @RabbitListener(queues = "${rabbitmq.queue.recommendation-request}")
    public void handleRecommendationRequest(InvestmentProfileDTO profile) {
        log.info("Received recommendation request from queue for user: {}, crypto: {}",
                profile.getUserId(), profile.getTargetCryptocurrency());

        try {
            // Process recommendation asynchronously
            recommendationService.processRecommendation(profile)
                    .doOnSuccess(response ->
                            log.info("Successfully processed async recommendation {} for user: {}",
                                    response.getRecommendationId(),
                                    profile.getUserId()))
                    .doOnError(error ->
                            log.error("Failed to process async recommendation for user: {}",
                                    profile.getUserId(), error))
                    .subscribe(); // Fire and forget

        } catch (Exception e) {
            log.error("Error handling recommendation request for user: {}",
                    profile.getUserId(), e);
            throw e; // Trigger retry mechanism
        }
    }
}
