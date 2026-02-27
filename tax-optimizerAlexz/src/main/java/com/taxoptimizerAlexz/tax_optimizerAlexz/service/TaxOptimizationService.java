package com.taxoptimizerAlexz.tax_optimizerAlexz.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxoptimizerAlexz.tax_optimizerAlexz.dto.*;
import com.taxoptimizerAlexz.tax_optimizerAlexz.messaging.TaxRecommendationMessage;
import com.taxoptimizerAlexz.tax_optimizerAlexz.messaging.TaxRecommendationProducer;
import com.taxoptimizerAlexz.tax_optimizerAlexz.model.TaxProfile;
import com.taxoptimizerAlexz.tax_optimizerAlexz.repo.TaxProfileRepository;
import com.taxoptimizerAlexz.tax_optimizerAlexz.repo.TaxRecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Reactive orchestrator service.
 *
 * Flow:
 *  1. Compute Old + New regime (synchronous — pure math, no I/O)
 *  2. Save TaxProfile to DB via R2DBC (reactive)
 *  3. Call Gemini AI (offloaded to boundedElastic via Mono.fromCallable)
 *  4. Build strategy list
 *  5. Publish TaxRecommendationMessage to RabbitMQ
 *  6. Consumer picks up message async → saves TaxRecommendation to DB
 *  7. Return TaxResponse to client
 */
@Service
@RequiredArgsConstructor
public class TaxOptimizationService {

    private static final Logger log = LoggerFactory.getLogger(TaxOptimizationService.class);

    private final TaxCalculationService       calculationService;
    private final GeminiService               geminiService;
    private final TaxProfileRepository        profileRepository;
    private final TaxRecommendationRepository recommendationRepository;
    private final TaxRecommendationProducer   producer;
    private final ObjectMapper                objectMapper;

    // ─── Main Entry Point (Reactive) ─────────────────────────────────────────

    public Mono<TaxResponse> processAndOptimize(TaxRequest request) {

        // Step 1: compute both regimes (blocking math, no I/O — OK on event loop)
        RegimeResult oldRegime = calculationService.calculateOldRegime(request);
        RegimeResult newRegime = calculationService.calculateNewRegime(request);

        boolean oldIsBetter   = oldRegime.getTotalTax() <= newRegime.getTotalTax();
        String  recommendedReg = oldIsBetter ? "Old Regime" : "New Regime";
        double  regimeSaving   = Math.abs(oldRegime.getTotalTax() - newRegime.getTotalTax());
        double  currentTax     = oldIsBetter ? oldRegime.getTotalTax() : newRegime.getTotalTax();

        // Step 2: generate rule-based strategies
        List<OptimizationStrategy> strategies = generateStrategies(request, oldRegime, newRegime);
        double potentialSavings = strategies.stream()
                .mapToDouble(OptimizationStrategy::getEstimatedTaxSaving).sum();
        double optimizedTax = Math.max(0, currentTax - potentialSavings);

        // Step 3: save profile reactively, then call Gemini, then publish to RabbitMQ
        return saveProfile(request)
                .flatMap(savedProfile -> {
                    log.info("[TaxService] Profile saved id={}", savedProfile.getId());

                    // Step 4: call Gemini AI (non-blocking via boundedElastic)
                    return geminiService.getTaxRecommendation(request, oldRegime, newRegime)
                            .map(aiText -> {
                                List<String> aiTips = geminiService.parseAiTips(aiText);

                                // Step 5: publish to RabbitMQ (async persistence)
                                publishToRabbitMQ(savedProfile.getId(), request, recommendedReg,
                                        oldRegime, newRegime, regimeSaving, potentialSavings,
                                        aiText, aiTips, strategies);

                                // Step 6: build and return response
                                return TaxResponse.builder()
                                        .profileId(savedProfile.getId())
                                        .oldRegimeResult(oldRegime)
                                        .newRegimeResult(newRegime)
                                        .recommendedRegime(recommendedReg)
                                        .taxSavingsWithRecommendation(regimeSaving)
                                        .currentEstimatedTax(currentTax)
                                        .optimizedEstimatedTax(optimizedTax)
                                        .potentialSavings(potentialSavings)
                                        .strategies(strategies)
                                        .aiRecommendation(aiText)
                                        .aiTips(aiTips)
                                        .summaryMessage(buildSummary(recommendedReg, regimeSaving,
                                                optimizedTax, potentialSavings))
                                        .recommendationStatus("QUEUED")
                                        .build();
                            });
                });
    }

    // ─── Fetch Saved Recommendation for a Profile ─────────────────────────────

    public Mono<com.taxoptimizerAlexz.tax_optimizerAlexz.model.TaxRecommendation> getRecommendationByProfile(Long profileId) {
        return recommendationRepository.findByProfileId(profileId);
    }

    public Flux<com.taxoptimizerAlexz.tax_optimizerAlexz.model.TaxRecommendation> getRecommendationsByUser(String userId) {
        return recommendationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // ─── Strategy Generator ───────────────────────────────────────────────────

    private List<OptimizationStrategy> generateStrategies(TaxRequest req,
                                                          RegimeResult old,
                                                          RegimeResult nw) {
        List<OptimizationStrategy> list  = new ArrayList<>();
        double marginalRate              = marginalRate(old.getTaxableIncome());

        // 80C gap
        double raw80C = req.getSection80C() + req.getHomeLoanPrincipal();
        double gap80C = 1_50_000 - Math.min(raw80C, 1_50_000);
        if (gap80C > 1000) {
            String instrument = pick80CInstrument(req.getRiskAppetite());
            list.add(OptimizationStrategy.builder()
                    .section("Section 80C")
                    .instrument(instrument)
                    .currentAmount(Math.min(raw80C, 1_50_000))
                    .recommendedAmount(1_50_000)
                    .additionalInvestment(gap80C)
                    .estimatedTaxSaving(gap80C * marginalRate * 1.04)
                    .rationale(String.format("₹%.0f unused 80C limit. Invest in %s to save ₹%.0f in tax.",
                            gap80C, instrument, gap80C * marginalRate * 1.04))
                    .build());
        }

        // NPS 80CCD(1B)
        double npsGap = 50_000 - Math.min(req.getSection80CCD1B(), 50_000);
        if (npsGap > 1000) {
            list.add(OptimizationStrategy.builder()
                    .section("Section 80CCD(1B)")
                    .instrument("National Pension System (NPS) — Tier I")
                    .currentAmount(req.getSection80CCD1B())
                    .recommendedAmount(50_000)
                    .additionalInvestment(npsGap)
                    .estimatedTaxSaving(npsGap * marginalRate * 1.04)
                    .rationale(String.format("NPS gives extra ₹50,000 deduction above 80C. " +
                            "Invest ₹%.0f more to save ₹%.0f.", npsGap, npsGap * marginalRate * 1.04))
                    .build());
        }

        // 80D Health Insurance
        double selfLimit     = req.getAge() >= 60 ? 50_000 : 25_000;
        double healthGap     = (selfLimit - Math.min(req.getSection80D(), selfLimit))
                + (50_000 - Math.min(req.getSection80DParents(), 50_000));
        if (healthGap > 500) {
            list.add(OptimizationStrategy.builder()
                    .section("Section 80D")
                    .instrument("Health Insurance (Self + Family + Parents)")
                    .currentAmount(req.getSection80D() + req.getSection80DParents())
                    .recommendedAmount(selfLimit + 50_000)
                    .additionalInvestment(healthGap)
                    .estimatedTaxSaving(healthGap * marginalRate * 1.04)
                    .rationale(String.format("₹%.0f unused health insurance deduction. " +
                            "Protects health + saves ₹%.0f tax.", healthGap, healthGap * marginalRate * 1.04))
                    .build());
        }

        // Regime switch advisory
        if (Math.abs(old.getTotalTax() - nw.getTotalTax()) > 5000) {
            boolean switchToNew = nw.getTotalTax() < old.getTotalTax();
            list.add(OptimizationStrategy.builder()
                    .section("Tax Regime Selection")
                    .instrument(switchToNew ? "Switch to New Regime" : "Stay in Old Regime")
                    .currentAmount(Math.max(old.getTotalTax(), nw.getTotalTax()))
                    .recommendedAmount(Math.min(old.getTotalTax(), nw.getTotalTax()))
                    .additionalInvestment(0)
                    .estimatedTaxSaving(Math.abs(old.getTotalTax() - nw.getTotalTax()))
                    .rationale(String.format("%s saves ₹%.0f annually. Select before filing ITR.",
                            switchToNew ? "New Regime" : "Old Regime",
                            Math.abs(old.getTotalTax() - nw.getTotalTax())))
                    .build());
        }

        return list;
    }

    // ─── RabbitMQ Publish ─────────────────────────────────────────────────────

    private void publishToRabbitMQ(Long profileId, TaxRequest request,
                                   String recommendedReg,
                                   RegimeResult oldRegime, RegimeResult newRegime,
                                   double regimeSaving, double potentialSavings,
                                   String aiText, List<String> aiTips,
                                   List<OptimizationStrategy> strategies) {
        try {
            String strategiesJson = objectMapper.writeValueAsString(strategies);

            TaxRecommendationMessage msg = TaxRecommendationMessage.builder()
                    .profileId(profileId)
                    .userId(request.getUserId())
                    .recommendedRegime(recommendedReg)
                    .oldRegimeTax(oldRegime.getTotalTax())
                    .newRegimeTax(newRegime.getTotalTax())
                    .taxSavingsRegime(regimeSaving)
                    .potentialSavings(potentialSavings)
                    .aiRecommendation(aiText)
                    .aiTips(aiTips)
                    .strategiesJson(strategiesJson)
                    .financialYear("2024-25")
                    .build();

            producer.publishRecommendation(msg);

        } catch (JsonProcessingException e) {
            log.error("[TaxService] Failed to serialize strategies for RabbitMQ publish: {}", e.getMessage());
        }
    }

    // ─── DB Save ──────────────────────────────────────────────────────────────

    private Mono<TaxProfile> saveProfile(TaxRequest req) {
        TaxProfile profile = TaxProfile.builder()
                .userId(req.getUserId())
                .age(req.getAge())
                .dependents(req.getDependents())
                .grossSalary(req.getGrossSalary())
                .basicSalary(req.getBasicSalary())
                .hra(req.getHra())
                .da(req.getDa())
                .specialAllowance(req.getSpecialAllowance())
                .otherIncome(req.getOtherIncome())
                .rentPaid(req.getRentPaid())
                .cityType(req.getCityType())
                .section80C(req.getSection80C())
                .section80D(req.getSection80D())
                .section80DParents(req.getSection80DParents())
                .section80CCD1B(req.getSection80CCD1B())
                .section80EEA(req.getSection80EEA())
                .section80G(req.getSection80G())
                .section80TTA(req.getSection80TTA())
                .homeLoanInterest(req.getHomeLoanInterest())
                .homeLoanPrincipal(req.getHomeLoanPrincipal())
                .riskAppetite(req.getRiskAppetite())
                .liquidityNeed(req.getLiquidityNeed())
                .createdAt(LocalDateTime.now())
                .build();
        return profileRepository.save(profile);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private double marginalRate(double taxableIncome) {
        if (taxableIncome <= 5_00_000)  return 0.05;
        if (taxableIncome <= 10_00_000) return 0.20;
        return 0.30;
    }

    private String pick80CInstrument(String riskAppetite) {
        return switch (riskAppetite.toUpperCase()) {
            case "HIGH"   -> "ELSS Mutual Fund (3-yr lock-in, equity)";
            case "LOW"    -> "NSC or 5-Year Tax Saver FD (safe, fixed)";
            default       -> "PPF (15-yr lock-in, 7.1% p.a. guaranteed)";
        };
    }

    private String buildSummary(String regime, double regimeSaving,
                                double optimizedTax, double potentialSavings) {
        return String.format(
                "%s is recommended — saves ₹%.0f over the other regime. " +
                        "By implementing the suggested strategies, you can save an additional ₹%.0f, " +
                        "bringing your estimated tax liability down to ₹%.0f.",
                regime, regimeSaving, potentialSavings, optimizedTax
        );
    }
}
