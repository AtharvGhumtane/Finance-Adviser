package com.taxoptimizerAlexz.tax_optimizerAlexz.dto;

import lombok.*;

/**
 * A single rule-based tax optimization strategy.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptimizationStrategy {

    private String section;                // e.g. "Section 80C"
    private String instrument;             // e.g. "ELSS Mutual Fund"
    private double currentAmount;
    private double recommendedAmount;
    private double additionalInvestment;
    private double estimatedTaxSaving;
    private String rationale;
}
