package com.creditcard.credit_card_service.service;

import com.creditcard.credit_card_service.dto.CreditRequest;
import com.creditcard.credit_card_service.dto.TrapResult;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Pure-Java Decision Tree-based Risk Classification Engine.
 *
 * Classifies users into: LOW | MEDIUM | HIGH risk
 *
 * Feature set (7 key predictors):
 *  F1: Credit Utilization %       (30% weight)
 *  F2: EMI Burden Ratio %         (20% weight)
 *  F3: Debt-to-Income Ratio %     (15% weight)
 *  F4: Late + Missed Payments     (15% weight)
 *  F5: Credit Score               (10% weight)
 *  F6: Traps Detected Count       (5%  weight)
 *  F7: Cash Advance Frequency     (5%  weight)
 *
 * Decision Tree logic:
 *
 *  Root: Is utilization > 75%?
 *   ├── YES → Is EMI burden > 65%?
 *   │     ├── YES → HIGH (Extreme Leverage)
 *   │     └── NO  → Is missed payments > 2?
 *   │                ├── YES → HIGH (Payment Default Risk)
 *   │                └── NO  → MEDIUM (Credit Overuse)
 *   └── NO → Is EMI burden > 50%?
 *         ├── YES → Is DTI > 60%?
 *         │     ├── YES → HIGH (Debt Overloaded)
 *         │     └── NO  → MEDIUM (EMI Stressed)
 *         └── NO → Is credit score < 600?
 *               ├── YES → Is late payments ≥ 3?
 *               │     ├── YES → HIGH (Credit Collapse)
 *               │     └── NO  → MEDIUM (Score Recovery)
 *               └── NO → Is utilization < 30% AND EMI burden < 40%?
 *                     ├── YES → LOW (Financially Healthy)
 *                     └── NO  → MEDIUM (Monitor)
 */
@Service
public class RiskClassificationService {

    public static final String LOW    = "LOW";
    public static final String MEDIUM = "MEDIUM";
    public static final String HIGH   = "HIGH";

    public record RiskResult(
            String riskLevel,
            int riskScore,
            String riskCategory,
            String reasoning
    ) {}

    public RiskResult classify(CreditRequest req, List<TrapResult> traps) {
        // ─── Compute all features ─────────────────────────────────────────────
        double utilization    = computeUtilization(req);
        double emiBurden      = computeEmiBurden(req);
        double dti            = computeDTI(req);
        int    paymentIssues  = req.getLatePaymentsLastYear() + req.getMissedPaymentsLastYear();
        int    trapsDetected  = (int) traps.stream().filter(TrapResult::isDetected).count();
        int    cashAdvFreq    = req.getCashAdvanceFrequency();

        // ─── Decision Tree Traversal ──────────────────────────────────────────
        String riskLevel;
        String reasoning;

        if (utilization > 75) {
            if (emiBurden > 65) {
                riskLevel = HIGH;
                reasoning = String.format(
                        "Critical double jeopardy: utilization at %.1f%% AND EMI burden at %.1f%%. " +
                                "You are borrowing heavily on credit while already over-leveraged on EMIs. " +
                                "Immediate debt restructuring required.", utilization, emiBurden);
            } else if (req.getMissedPaymentsLastYear() > 2) {
                riskLevel = HIGH;
                reasoning = String.format(
                        "High utilization (%.1f%%) combined with %d missed payments indicates " +
                                "inability to service current debt levels. Risk of default is elevated.",
                        utilization, req.getMissedPaymentsLastYear());
            } else {
                riskLevel = MEDIUM;
                reasoning = String.format(
                        "Credit utilization at %.1f%% is in the danger zone (>75%%). " +
                                "While payments are being made, this level of credit usage significantly " +
                                "damages your CIBIL score and signals financial stress to lenders.", utilization);
            }
        } else if (emiBurden > 50) {
            if (dti > 60) {
                riskLevel = HIGH;
                reasoning = String.format(
                        "Severely over-leveraged: EMI burden %.1f%% of income and debt-to-income ratio " +
                                "at %.1f%%. Less than 40%% of income available for living expenses. " +
                                "Any income disruption will cause immediate default.", emiBurden, dti);
            } else {
                riskLevel = MEDIUM;
                reasoning = String.format(
                        "EMI burden at %.1f%% of income exceeds the 50%% safety limit. " +
                                "Cash flow is under pressure. Avoid taking on any new debt. " +
                                "Focus on foreclose of high-interest obligations.", emiBurden);
            }
        } else if (req.getCreditScore() < 600) {
            if (paymentIssues >= 3) {
                riskLevel = HIGH;
                reasoning = String.format(
                        "Poor credit score (%d) combined with %d payment issues in the past year " +
                                "signals a serious credit collapse pattern. Banks will reject new credit " +
                                "applications and may impose higher rates on existing products.",
                        req.getCreditScore(), paymentIssues);
            } else {
                riskLevel = MEDIUM;
                reasoning = String.format(
                        "Credit score of %d is in the poor range (<600). This limits access to " +
                                "credit products. With %d payment issues, the score can still be rehabilitated " +
                                "with 6–12 months of disciplined payment behavior.",
                        req.getCreditScore(), paymentIssues);
            }
        } else if (utilization <= 30 && emiBurden <= 40 && paymentIssues == 0 && dti <= 40) {
            riskLevel = LOW;
            reasoning = String.format(
                    "Excellent credit profile: utilization %.1f%%, EMI burden %.1f%%, " +
                            "zero payment issues, and DTI %.1f%%. You demonstrate disciplined " +
                            "credit management and have significant financial headroom.",
                    utilization, emiBurden, dti);
        } else {
            riskLevel = MEDIUM;
            reasoning = String.format(
                    "Moderate risk profile: utilization %.1f%%, EMI burden %.1f%%, DTI %.1f%%, " +
                            "%d payment issues, %d traps detected. Your finances are manageable but " +
                            "require active monitoring and improvement in highlighted areas.",
                    utilization, emiBurden, dti, paymentIssues, trapsDetected);
        }

        // ─── Compute composite risk score (0–100) ─────────────────────────────
        int score = computeRiskScore(utilization, emiBurden, dti, paymentIssues,
                req.getCreditScore(), trapsDetected, cashAdvFreq);

        String category = switch (riskLevel) {
            case LOW    -> "Financially Stable";
            case MEDIUM -> "Financial Monitor Required";
            case HIGH   -> "Danger Zone — Immediate Action";
            default     -> "Unknown";
        };

        return new RiskResult(riskLevel, score, category, reasoning);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Composite Risk Score Calculator (0 = safest, 100 = most dangerous)
    // ─────────────────────────────────────────────────────────────────────────
    private int computeRiskScore(double utilization, double emiBurden, double dti,
                                 int paymentIssues, int creditScore,
                                 int trapsDetected, int cashAdvFreq) {
        double score = 0;

        // F1: Utilization (weight 30)
        score += Math.min(utilization / 100.0, 1.0) * 30;

        // F2: EMI Burden (weight 20)
        score += Math.min(emiBurden / 100.0, 1.0) * 20;

        // F3: DTI (weight 15)
        score += Math.min(dti / 100.0, 1.0) * 15;

        // F4: Payment Issues (weight 15) — normalize over 12
        score += Math.min(paymentIssues / 12.0, 1.0) * 15;

        // F5: Credit Score (weight 10) — inverse: lower score = higher risk
        double normalizedScore = Math.max(0, Math.min(1.0, (900 - creditScore) / 600.0));
        score += normalizedScore * 10;

        // F6: Traps detected (weight 5) — normalize over 6
        score += Math.min(trapsDetected / 6.0, 1.0) * 5;

        // F7: Cash advance frequency (weight 5) — normalize over 4
        score += Math.min(cashAdvFreq / 4.0, 1.0) * 5;

        return (int) Math.round(Math.min(score, 100));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Feature Helpers
    // ─────────────────────────────────────────────────────────────────────────

    public double computeUtilization(CreditRequest req) {
        return req.getTotalCreditLimit() > 0
                ? (req.getTotalOutstandingBalance() / req.getTotalCreditLimit()) * 100.0
                : 0.0;
    }

    public double computeEmiBurden(CreditRequest req) {
        double totalEmi = req.getTotalEmiPerMonth() + req.getOtherLoanEmi();
        return req.getMonthlyIncome() > 0
                ? (totalEmi / req.getMonthlyIncome()) * 100.0
                : 0.0;
    }

    public double computeDTI(CreditRequest req) {
        double annualDebt   = (req.getTotalEmiPerMonth() + req.getOtherLoanEmi()) * 12
                + req.getTotalOutstandingBalance();
        double annualIncome = req.getMonthlyIncome() * 12;
        return annualIncome > 0 ? (annualDebt / annualIncome) * 100.0 : 0.0;
    }
}