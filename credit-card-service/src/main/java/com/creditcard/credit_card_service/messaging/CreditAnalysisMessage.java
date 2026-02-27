package com.creditcard.credit_card_service.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * RabbitMQ message payload for async analysis persistence.
 * Published after the HTTP response is returned to the client.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditAnalysisMessage implements Serializable {

    private Long profileId;
    private String userId;

    // Risk classification
    private String riskLevel;
    private int riskScore;
    private String riskReasoning;

    // Trap detection
    private int trapsDetectedCount;
    private String trapsJson;

    // Health metrics
    private double creditUtilizationPct;
    private double debtToIncomeRatio;
    private double emiBurdenRatio;
    private double estimatedAnnualInterest;
    private double freeCashFlow;

    // AI output
    private String aiRecommendation;
    private String aiTipsJson;
}
