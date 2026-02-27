package com.creditcard.credit_card_service.messaging;

import com.creditcard.credit_card_service.model.CreditAnalysis;
import com.creditcard.credit_card_service.repo.CreditAnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Consumes credit analysis messages from RabbitMQ and persists
 * the full analysis to PostgreSQL via R2DBC.
 *
 * This runs completely async — HTTP response was already sent.
 * Failed messages are automatically routed to DLQ.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreditAnalysisConsumer {

    private final CreditAnalysisRepository analysisRepository;

    @RabbitListener(queues = "${rabbitmq.queue.credit-analysis}")
    public void consumeAnalysis(CreditAnalysisMessage message) {
        log.info("Received analysis message from RabbitMQ for profile: {}", message.getProfileId());

        CreditAnalysis entity = CreditAnalysis.builder()
                .profileId(message.getProfileId())
                .userId(message.getUserId())
                .riskLevel(message.getRiskLevel())
                .riskScore(message.getRiskScore())
                .riskReasoning(message.getRiskReasoning())
                .trapsDetectedCount(message.getTrapsDetectedCount())
                .trapsJson(message.getTrapsJson())
                .creditUtilizationPct(message.getCreditUtilizationPct())
                .debtToIncomeRatio(message.getDebtToIncomeRatio())
                .emiBurdenRatio(message.getEmiBurdenRatio())
                .estimatedAnnualInterest(message.getEstimatedAnnualInterest())
                .freeCashFlow(message.getFreeCashFlow())
                .aiRecommendation(message.getAiRecommendation())
                .aiTipsJson(message.getAiTipsJson())
                .status("GENERATED")
                .createdAt(LocalDateTime.now())
                .build();

        // Block needed here: @RabbitListener runs on traditional thread pool
        analysisRepository.save(entity)
                .doOnSuccess(saved -> log.info("Saved credit analysis to DB. ID: {} for user: {}",
                        saved.getId(), saved.getUserId()))
                .doOnError(e -> log.error("Failed to save analysis for profile {}: {}",
                        message.getProfileId(), e.getMessage()))
                .subscribe();
    }
}
