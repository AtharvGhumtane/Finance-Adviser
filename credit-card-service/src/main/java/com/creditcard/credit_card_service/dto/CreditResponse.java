package com.creditcard.credit_card_service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Full API response — includes ML risk classification, all detected traps,
 * financial health metrics, and Gemini AI recommendations.
 *
 * This is returned immediately to the HTTP client.
 * DB persistence happens async via RabbitMQ.
 */
@Data
@Builder
public class CreditResponse {

    private Long profileId;
    private String userId;

    // ─── ML Risk Classification (Decision Tree) ───────────────────────────────

    private String riskLevel;          // LOW | MEDIUM | HIGH
    private int riskScore;             // 0–100 composite score
    private String riskCategory;       // Human-readable: "Financially Stable", "At Risk", "Danger Zone"
    private String riskReasoning;      // Explanation of ML decision

    // ─── Trap Detection Summary ───────────────────────────────────────────────

    private int trapsDetectedCount;
    private int totalTrapsChecked;
    private List<TrapResult> detectedTraps;
    private List<TrapResult> allTrapResults;   // All 6 including non-detected

    // ─── Financial Health Metrics ─────────────────────────────────────────────

    private FinancialHealthMetrics healthMetrics;

    // ─── AI Recommendations (Gemini) ─────────────────────────────────────────

    private String aiRecommendation;
    private List<String> aiTips;
    private String urgencyLevel;       // "Immediate Action" | "Monitor" | "On Track"

    // ─── Metadata ─────────────────────────────────────────────────────────────

    private LocalDateTime analysisTimestamp;
    private String disclaimer;
}
