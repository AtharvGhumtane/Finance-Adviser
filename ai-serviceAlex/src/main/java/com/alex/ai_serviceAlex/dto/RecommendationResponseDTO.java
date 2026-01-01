package com.alex.ai_serviceAlex.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponseDTO {

    private Long recommendationId;
    private UUID userId;
    private String targetCryptocurrency;
    private String recommendationText;
    private Double confidenceScore;
    private String riskAssessment;
    private String allocationSuggestion;
    private LocalDateTime createdAt;
    private Long processingTimeMs;

    // Summary fields
    private String investmentStrategy;
    private String timeframe;
    private String keyInsights;
}
