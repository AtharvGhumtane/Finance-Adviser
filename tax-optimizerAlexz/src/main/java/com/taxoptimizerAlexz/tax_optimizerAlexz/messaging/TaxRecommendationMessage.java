package com.taxoptimizerAlexz.tax_optimizerAlexz.messaging;

import lombok.*;
import java.io.Serializable;
import java.util.List;

/**
 * Message payload published to RabbitMQ after tax optimization is computed.
 * Consumer picks this up and persists it to PostgreSQL via R2DBC.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxRecommendationMessage implements Serializable {

    private Long   profileId;
    private String userId;

    private String recommendedRegime;
    private double oldRegimeTax;
    private double newRegimeTax;
    private double taxSavingsRegime;
    private double potentialSavings;

    private String       aiRecommendation;
    private List<String> aiTips;
    private String       strategiesJson;     // Serialized OptimizationStrategy list
    private String       financialYear;      // "2024-25"
}
