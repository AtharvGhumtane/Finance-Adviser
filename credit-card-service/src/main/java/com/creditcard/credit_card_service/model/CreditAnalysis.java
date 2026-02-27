package com.creditcard.credit_card_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("credit_analyses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditAnalysis {

    @Id
    private Long id;

    @Column("profile_id")
    private Long profileId;

    @Column("user_id")
    private String userId;

    // ML Risk
    @Column("risk_level")
    private String riskLevel;

    @Column("risk_score")
    private int riskScore;

    @Column("risk_reasoning")
    private String riskReasoning;

    // Traps
    @Column("traps_detected_count")
    private int trapsDetectedCount;

    @Column("traps_json")
    private String trapsJson;

    // Health Metrics
    @Column("credit_utilization_pct")
    private double creditUtilizationPct;

    @Column("debt_to_income_ratio")
    private double debtToIncomeRatio;

    @Column("emi_burden_ratio")
    private double emiBurdenRatio;

    @Column("estimated_annual_interest")
    private double estimatedAnnualInterest;

    @Column("free_cash_flow")
    private double freeCashFlow;

    // AI
    @Column("ai_recommendation")
    private String aiRecommendation;

    @Column("ai_tips_json")
    private String aiTipsJson;

    @Column("status")
    private String status;

    @Column("created_at")
    private LocalDateTime createdAt;
}
