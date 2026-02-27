package com.taxoptimizerAlexz.tax_optimizerAlexz.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * R2DBC entity — maps to 'tax_recommendations' table in PostgreSQL.
 *
 * Saved asynchronously via RabbitMQ consumer after Gemini AI generates
 * the recommendation. Linked to a TaxProfile via profileId.
 */
@Table("tax_recommendations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxRecommendation {

    @Id
    private Long id;

    @Column("profile_id")
    private Long profileId;              // FK → tax_profiles.id

    @Column("user_id")
    private String userId;

    @Column("recommended_regime")
    private String recommendedRegime;    // "Old Regime" | "New Regime"

    @Column("old_regime_tax")
    private double oldRegimeTax;

    @Column("new_regime_tax")
    private double newRegimeTax;

    @Column("tax_savings_regime")
    private double taxSavingsRegime;     // Regime switch savings

    @Column("potential_savings")
    private double potentialSavings;     // Total savings from strategies

    @Column("ai_recommendation")
    private String aiRecommendation;     // Full Gemini response text

    @Column("strategies_json")
    private String strategiesJson;       // JSON string of OptimizationStrategy list

    @Column("financial_year")
    private String financialYear;        // e.g. "2024-25"

    @Column("status")
    private String status;               // "GENERATED" | "VIEWED" | "APPLIED"

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;
}
