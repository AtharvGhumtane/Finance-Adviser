package com.taxoptimizerAlexz.tax_optimizerAlexz.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.taxoptimizerAlexz.tax_optimizerAlexz.dto.RegimeResult;
import com.taxoptimizerAlexz.tax_optimizerAlexz.dto.TaxRequest;
import com.taxoptimizerAlexz.tax_optimizerAlexz.exception.GeminiServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gemini AI Service — uses google-genai SDK 1.0.0.
 *
 * Calls Gemini 1.5 Flash (free tier) with a structured prompt built
 * from the taxpayer's profile and computed tax results.
 *
 * Wrapped in Mono.fromCallable + subscribeOn(boundedElastic) so the
 * blocking SDK call doesn't block the WebFlux event loop.
 */
@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    @Value("${gemini.api.model}")
    private String modelName;

    private final Client geminiClient;

    public GeminiService(Client geminiClient) {
        this.geminiClient = geminiClient;
    }

    /**
     * Calls Gemini API reactively (blocking call offloaded to boundedElastic thread pool).
     *
     * @param request   Taxpayer's profile
     * @param oldResult Old regime computation
     * @param newResult New regime computation
     * @return Mono<String> — AI-generated recommendation text
     */
    public Mono<String> getTaxRecommendation(TaxRequest request,
                                             RegimeResult oldResult,
                                             RegimeResult newResult) {
        return Mono.fromCallable(() -> callGemini(request, oldResult, newResult))
                .subscribeOn(Schedulers.boundedElastic())  // offload blocking SDK call
                .doOnSuccess(r -> log.info("[Gemini] Recommendation generated for userId={}", request.getUserId()))
                .onErrorMap(e -> new GeminiServiceException("Gemini API call failed: " + e.getMessage(), e));
    }

    /**
     * Parses the numbered/bulleted tip list from Gemini's text response.
     */
    public List<String> parseAiTips(String aiText) {
        if (aiText == null || aiText.isBlank()) return Collections.emptyList();

        List<String> tips = new ArrayList<>();
        for (String line : aiText.split("\n")) {
            String cleaned = line.trim()
                    .replaceAll("^[0-9]+\\.\\s*", "")       // Remove "1. "
                    .replaceAll("^[•\\-*]\\s*", "")          // Remove "• - *"
                    .replaceAll("\\*\\*(.*?)\\*\\*", "$1");  // Remove **bold**
            if (!cleaned.isBlank() && cleaned.length() > 15) {
                tips.add(cleaned);
            }
        }
        return tips;
    }

    // ─── Private ──────────────────────────────────────────────────────────────

    private String callGemini(TaxRequest req, RegimeResult old, RegimeResult nw) {
        try {
            String prompt = buildPrompt(req, old, nw);
            GenerateContentResponse response = geminiClient.models.generateContent(
                    modelName,
                    prompt,
                    null
            );
            return response.text();
        } catch (Exception e) {
            log.error("[Gemini] API error for userId={}: {}", req.getUserId(), e.getMessage(), e);
            return "AI recommendation temporarily unavailable. Please rely on the rule-based strategies above.";
        }
    }

    private String buildPrompt(TaxRequest req, RegimeResult old, RegimeResult nw) {
        return String.format("""
                You are an expert Indian tax advisor for FY 2024-25. Analyze the taxpayer profile below and provide clear, actionable, personalized tax-saving advice in simple English.

                === TAXPAYER PROFILE ===
                Age: %d years | Dependents: %d | City: %s
                Gross Salary: ₹%.0f | Basic: ₹%.0f | HRA: ₹%.0f
                Rent Paid: ₹%.0f | Other Income: ₹%.0f
                Risk Appetite: %s | Liquidity Need: %s

                === CURRENT DEDUCTIONS ===
                80C Investments: ₹%.0f / ₹1,50,000
                NPS 80CCD(1B):   ₹%.0f / ₹50,000
                Health Ins (Self): ₹%.0f | Parents: ₹%.0f
                Home Loan Interest: ₹%.0f
                Donations 80G: ₹%.0f

                === TAX RESULTS ===
                Old Regime → Taxable: ₹%.0f | Tax: ₹%.0f | Rate: %.1f%%
                New Regime → Taxable: ₹%.0f | Tax: ₹%.0f | Rate: %.1f%%

                Please provide:
                1. Which regime to choose and exact tax saving amount
                2. Top 5 personalized strategies with specific instruments and amounts
                3. Missed deduction opportunities
                4. 3-month action plan
                5. Key compliance reminders

                Be specific with ₹ amounts. Use numbered list format. Keep language simple.
                """,
                req.getAge(), req.getDependents(), req.getCityType(),
                req.getGrossSalary(), req.getBasicSalary(), req.getHra(),
                req.getRentPaid(), req.getOtherIncome(),
                req.getRiskAppetite(), req.getLiquidityNeed(),
                req.getSection80C(), req.getSection80CCD1B(),
                req.getSection80D(), req.getSection80DParents(),
                req.getHomeLoanInterest(), req.getSection80G(),
                old.getTaxableIncome(), old.getTotalTax(), old.getEffectiveTaxRate(),
                nw.getTaxableIncome(),  nw.getTotalTax(),  nw.getEffectiveTaxRate()
        );
    }
}
