package com.creditcard.credit_card_service.service;

import com.creditcard.credit_card_service.dto.CreditRequest;
import com.creditcard.credit_card_service.dto.TrapResult;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Wraps the blocking Gemini SDK call in boundedElastic scheduler
 * so it never blocks the reactive event loop.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    private final Client geminiClient;

    @Value("${gemini.api.model:gemini-1.5-flash}")
    private String modelName;

    public Mono<String> getCreditRecommendation(
            CreditRequest req,
            List<TrapResult> traps,
            RiskClassificationService.RiskResult risk,
            double utilization,
            double emiBurden,
            double dti,
            double freeCashFlow) {

        return Mono.fromCallable(() -> callGemini(req, traps, risk, utilization, emiBurden, dti, freeCashFlow))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(e -> log.error("Gemini API call failed: {}", e.getMessage()))
                .onErrorReturn(buildFallbackRecommendation(traps, risk));
    }

    private String callGemini(
            CreditRequest req,
            List<TrapResult> traps,
            RiskClassificationService.RiskResult risk,
            double utilization, double emiBurden, double dti, double freeCashFlow) throws Exception {

        String prompt = buildPrompt(req, traps, risk, utilization, emiBurden, dti, freeCashFlow);
        GenerateContentResponse response = geminiClient.models.generateContent(modelName, prompt, null);
        return response.text();
    }

    private String buildPrompt(
            CreditRequest req,
            List<TrapResult> traps,
            RiskClassificationService.RiskResult risk,
            double utilization, double emiBurden, double dti, double freeCashFlow) {

        String detectedTraps = traps.stream()
                .filter(TrapResult::isDetected)
                .map(t -> String.format("  - %s [%s]: %s", t.getTrapName(), t.getSeverity(), t.getConsequence()))
                .collect(Collectors.joining("\n"));

        return String.format("""
            You are a certified financial advisor specializing in Indian personal finance and credit management.
            Analyze this credit card risk profile and provide explainable, personalized recommendations.

            === USER PROFILE ===
            Monthly Income:    ₹%.0f
            Monthly Expenses:  ₹%.0f
            Free Cash Flow:    ₹%.0f
            Credit Score:      %d
            Number of Cards:   %d

            === CREDIT CARD STATUS ===
            Total Credit Limit:     ₹%.0f
            Outstanding Balance:    ₹%.0f
            Credit Utilization:     %.1f%%
            Annual Interest Rate:   %.1f%%

            === DEBT OBLIGATIONS ===
            Card EMI per Month:     ₹%.0f
            Other Loan EMI:         ₹%.0f
            EMI Burden Ratio:       %.1f%% of income
            Debt-to-Income Ratio:   %.1f%%

            === PAYMENT BEHAVIOR ===
            Pays Minimum Only:      %s
            Late Payments (1yr):    %d
            Missed Payments (1yr):  %d
            Cash Advance Amount:    ₹%.0f (Frequency: %d/month)

            === ML RISK CLASSIFICATION ===
            Risk Level:    %s
            Risk Score:    %d/100
            ML Reasoning:  %s

            === DETECTED TRAPS (%d detected) ===
            %s

            === YOUR TASK ===
            Provide a comprehensive, compassionate, and actionable credit health report with:

            1. EXECUTIVE SUMMARY (2–3 sentences): Overall financial health assessment
            2. TRAP ANALYSIS: For each detected trap, explain WHY it's dangerous in simple terms with real INR numbers
            3. PRIORITY ACTION PLAN: Top 5 specific steps ranked by urgency with expected impact
            4. DEBT ELIMINATION STRATEGY: Concrete payoff plan with timelines
            5. CREDIT SCORE RECOVERY: Steps to improve CIBIL score within 6–12 months
            6. BEHAVIORAL CHANGE TIPS: 3–5 specific habits to adopt immediately
            7. POSITIVE REINFORCEMENT: Acknowledge what the user is doing right

            Use simple, clear language. Reference Indian banking context (CIBIL, RBI guidelines).
            Provide specific INR amounts where possible. Be empathetic but direct.
            """,
                req.getMonthlyIncome(), req.getMonthlyExpenses(), freeCashFlow,
                req.getCreditScore(), req.getNumberOfCards(),
                req.getTotalCreditLimit(), req.getTotalOutstandingBalance(), utilization,
                req.getAnnualInterestRate(),
                req.getTotalEmiPerMonth(), req.getOtherLoanEmi(), emiBurden, dti,
                req.isPaysMinimumOnly() ? "YES ⚠️" : "No",
                req.getLatePaymentsLastYear(), req.getMissedPaymentsLastYear(),
                req.getCashAdvanceAmount(), req.getCashAdvanceFrequency(),
                risk.riskLevel(), risk.riskScore(), risk.reasoning(),
                traps.stream().filter(TrapResult::isDetected).count(),
                detectedTraps.isEmpty() ? "  No traps detected — excellent financial behavior!" : detectedTraps
        );
    }

    private String buildFallbackRecommendation(List<TrapResult> traps, RiskClassificationService.RiskResult risk) {
        long trapCount = traps.stream().filter(TrapResult::isDetected).count();
        return String.format("""
                Based on your analysis, your risk level is %s with a score of %d/100.

                %d credit trap(s) were detected in your profile.

                KEY RECOMMENDATIONS:
                1. Keep credit utilization below 30%% of your total limit
                2. Always pay more than the minimum due — ideally the full statement balance
                3. Never use credit card cash advances; use personal loans instead
                4. Ensure total EMIs don't exceed 50%% of your monthly income
                5. Set up auto-pay to avoid late payment fees and credit score damage
                6. Check your CIBIL score monthly at cibil.com

                %s
                """,
                risk.riskLevel(), risk.riskScore(), trapCount, risk.reasoning());
    }
}
