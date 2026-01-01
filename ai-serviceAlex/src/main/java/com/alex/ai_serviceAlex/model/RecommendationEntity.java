package com.alex.ai_serviceAlex.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("recommendations")
public class RecommendationEntity {

    @Id
    private Long id;

    @Column("user_id")
    private UUID userId;

    @Column("target_cryptocurrency")
    private String targetCryptocurrency;

    @Column("annual_income")
    private BigDecimal annualIncome;


    @Column("risk_tolerance")
    private Integer riskTolerance;

    @Column("investment_horizon")
    private String investmentHorizon;

    @Column("recommendation_text")
    private String recommendationText;

    @Column("confidence_score")
    private Double confidenceScore;

    @Column("risk_assessment")
    private String riskAssessment;

    @Column("processing_time_ms")
    private Long processingTimeMs;

    @Column("ai_model_version")
    private String aiModelVersion;

    @Column("created_at")
    private LocalDateTime createdAt;
}
