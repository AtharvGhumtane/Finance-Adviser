package com.taxoptimizerAlexz.tax_optimizerAlexz.dto;

import lombok.*;
import java.util.List;

/**
 * Complete tax optimization response returned to the client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxResponse {

    private Long   profileId;

    // ─── Regime Comparison ────────────────────────────────────────────────────
    private RegimeResult oldRegimeResult;
    private RegimeResult newRegimeResult;
    private String       recommendedRegime;
    private double       taxSavingsWithRecommendation;

    // ─── Before / After Simulation ────────────────────────────────────────────
    private double currentEstimatedTax;
    private double optimizedEstimatedTax;
    private double potentialSavings;

    // ─── Rule-Based Strategies ────────────────────────────────────────────────
    private List<OptimizationStrategy> strategies;

    // ─── Gemini AI ────────────────────────────────────────────────────────────
    private String       aiRecommendation;
    private List<String> aiTips;

    // ─── Summary ─────────────────────────────────────────────────────────────
    private String summaryMessage;

    // ─── Async Status ────────────────────────────────────────────────────────
    private String recommendationStatus;  // "QUEUED" — saved async via RabbitMQ
}
