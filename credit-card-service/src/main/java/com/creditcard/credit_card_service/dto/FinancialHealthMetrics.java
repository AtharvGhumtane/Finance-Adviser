package com.creditcard.credit_card_service.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Key financial health indicators computed from the user's credit profile.
 * Also includes "recommended" scenario values for comparison simulation.
 */
@Data
@Builder
public class FinancialHealthMetrics {

    // ─── Core Ratios ──────────────────────────────────────────────────────────

    private double creditUtilizationPct;        // Outstanding / Limit × 100
    private double recommendedUtilizationPct;   // Should be ≤ 30%

    private double debtToIncomeRatio;           // (All EMI + Balance) / Monthly Income × 100
    private double safeDebtToIncomeRatio;       // Should be ≤ 40%

    private double emiBurdenRatio;              // Total EMI / Monthly Income × 100
    private double safeEmiBurdenRatio;          // Should be ≤ 50%

    // ─── Interest Cost Simulation ─────────────────────────────────────────────

    private double estimatedMonthlyInterest;    // Interest on outstanding balance
    private double estimatedAnnualInterest;     // Annual cost of carrying this debt
    private double interestIfMinimumOnly;        // Cost of only paying minimum for 1 year

    // ─── Cash Flow ────────────────────────────────────────────────────────────

    private double grossMonthlyCashFlow;        // Income - Expenses
    private double netFreeCashFlow;             // After all EMIs and obligations

    // ─── Scenario Simulation: Current vs Recommended ──────────────────────────

    private double currentMonthlyDebtPayment;
    private double recommendedMonthlyDebtPayment;
    private double monthlySavingIfOptimized;    // Potential saving with recommended behavior

    // ─── Credit Score Impact ──────────────────────────────────────────────────

    private int currentCreditScore;
    private int projectedScoreIfCorrected;      // Estimated score after fixing detected traps

    // ─── Summary Flags ────────────────────────────────────────────────────────

    private boolean isUtilizationHealthy;
    private boolean isDtiHealthy;
    private boolean isEmiBurdenHealthy;
    private boolean isCashFlowPositive;
}
