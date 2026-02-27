package com.creditcard.credit_card_service.service;

import com.creditcard.credit_card_service.dto.*;
import com.creditcard.credit_card_service.messaging.CreditAnalysisMessage;
import com.creditcard.credit_card_service.messaging.CreditAnalysisProducer;
import com.creditcard.credit_card_service.model.CreditProfile;
import com.creditcard.credit_card_service.repo.CreditAnalysisRepository;
import com.creditcard.credit_card_service.repo.CreditProfileRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main reactive orchestrator for the credit card analysis pipeline.
 *
 * Flow:
 *  1. Map CreditRequest → CreditProfile entity → save via R2DBC
 *  2. Run 6-trap rule engine (sync, fast)
 *  3. Run Decision Tree risk classification (sync, fast)
 *  4. Compute all financial health metrics
 *  5. Call Gemini AI for explainable recommendations (async, offloaded)
 *  6. Build and return CreditResponse to HTTP client
 *  7. Publish to RabbitMQ → Consumer saves CreditAnalysis to DB async
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreditCardService {

    private final CreditProfileRepository profileRepo;
    private final CreditAnalysisRepository analysisRepo;
    private final TrapDetectionService trapService;
    private final RiskClassificationService riskService;
    private final GeminiService geminiService;
    private final CreditAnalysisProducer producer;
    private final ObjectMapper objectMapper;

    // ─────────────────────────────────────────────────────────────────────────
    // MAIN: Full Credit Trap Analysis + AI Recommendation
    // ─────────────────────────────────────────────────────────────────────────
    public Mono<CreditResponse> analyzeAndRecommend(CreditRequest req) {
        log.info("Starting credit analysis for user: {}", req.getUserId());

        // Step 1: Run rule engine and ML (synchronous, instant)
        List<TrapResult> allTraps = trapService.detectAllTraps(req);
        RiskClassificationService.RiskResult risk = riskService.classify(req, allTraps);

        // Step 2: Compute financial metrics
        FinancialHealthMetrics metrics = computeHealthMetrics(req, risk);

        // Step 3: Save profile to DB
        return profileRepo.save(buildProfile(req))
                .flatMap(savedProfile -> {

                    // Step 4: Get Gemini recommendation
                    return geminiService.getCreditRecommendation(
                            req, allTraps, risk,
                            metrics.getCreditUtilizationPct(),
                            metrics.getEmiBurdenRatio(),
                            metrics.getDebtToIncomeRatio(),
                            metrics.getNetFreeCashFlow()
                    ).flatMap(aiText -> {

                        List<String> aiTips = extractTips(aiText);

                        // Step 5: Build HTTP response
                        CreditResponse response = buildResponse(
                                savedProfile.getId(), req, allTraps, risk, metrics, aiText, aiTips);

                        // Step 6: Publish async to RabbitMQ
                        publishAsync(savedProfile.getId(), req, allTraps, risk, metrics, aiText, aiTips);

                        log.info("Analysis complete for user: {} → Risk: {} ({})",
                                req.getUserId(), risk.riskLevel(), risk.riskScore());

                        return Mono.just(response);
                    });
                });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Quick trap check (no DB, no AI)
    // ─────────────────────────────────────────────────────────────────────────
    public Mono<List<TrapResult>> quickTrapCheck(CreditRequest req) {
        return Mono.just(trapService.detectAllTraps(req));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Quick risk classification only (no DB, no AI)
    // ─────────────────────────────────────────────────────────────────────────
    public Mono<RiskClassificationService.RiskResult> classifyRiskOnly(CreditRequest req) {
        List<TrapResult> traps = trapService.detectAllTraps(req);
        return Mono.just(riskService.classify(req, traps));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fetch profile by ID
    // ─────────────────────────────────────────────────────────────────────────
    public Mono<CreditProfile> getProfileById(Long id) {
        return profileRepo.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Profile not found: " + id)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fetch all profiles for a user
    // ─────────────────────────────────────────────────────────────────────────
    public Flux<CreditProfile> getProfilesByUser(String userId) {
        return profileRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fetch analysis result for a profile
    // ─────────────────────────────────────────────────────────────────────────
    public Mono<CreditResponse> getAnalysisByProfileId(Long profileId) {
        return analysisRepo.findByProfileId(profileId)
                .switchIfEmpty(Mono.error(new RuntimeException("Analysis not yet available for profile: " + profileId)))
                .flatMap(analysis -> profileRepo.findById(analysis.getProfileId())
                        .map(profile -> {
                            List<TrapResult> traps = parseTrapJson(analysis.getTrapsJson());
                            List<String> aiTips = parseAiTipsJson(analysis.getAiTipsJson());
                            return CreditResponse.builder()
                                    .profileId(profileId)
                                    .userId(analysis.getUserId())
                                    .riskLevel(analysis.getRiskLevel())
                                    .riskScore(analysis.getRiskScore())
                                    .riskCategory(getRiskCategory(analysis.getRiskLevel()))
                                    .riskReasoning(analysis.getRiskReasoning())
                                    .trapsDetectedCount(analysis.getTrapsDetectedCount())
                                    .totalTrapsChecked(6)
                                    .detectedTraps(traps.stream().filter(TrapResult::isDetected).toList())
                                    .allTrapResults(traps)
                                    .aiRecommendation(analysis.getAiRecommendation())
                                    .aiTips(aiTips)
                                    .analysisTimestamp(analysis.getCreatedAt())
                                    .disclaimer("This analysis is for financial awareness purposes only.")
                                    .build();
                        }));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fetch all analyses for a user
    // ─────────────────────────────────────────────────────────────────────────
    public Flux<com.creditcard.credit_card_service.model.CreditAnalysis> getAnalysesByUser(String userId) {
        return analysisRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Update analysis status
    // ─────────────────────────────────────────────────────────────────────────
    public Mono<Void> updateStatus(Long id, String status) {
        return analysisRepo.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Analysis not found: " + id)))
                .flatMap(analysis -> {
                    analysis.setStatus(status);
                    return analysisRepo.save(analysis).then();
                });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private FinancialHealthMetrics computeHealthMetrics(CreditRequest req,
                                                        RiskClassificationService.RiskResult risk) {
        double utilization  = riskService.computeUtilization(req);
        double emiBurden    = riskService.computeEmiBurden(req);
        double dti          = riskService.computeDTI(req);

        double totalEmi     = req.getTotalEmiPerMonth() + req.getOtherLoanEmi();
        double monthlyRate  = req.getAnnualInterestRate() / 1200.0;

        double monthlyInterest      = req.getTotalOutstandingBalance() * monthlyRate;
        double annualInterest       = monthlyInterest * 12;
        double interestIfMinOnly    = req.getTotalOutstandingBalance()
                * (req.getAnnualInterestRate() / 100.0); // 1 year if stagnant

        double grossCashFlow   = req.getMonthlyIncome() - req.getMonthlyExpenses();
        double netFreeCashFlow = grossCashFlow - totalEmi;

        double idealBalance    = req.getTotalCreditLimit() * 0.30;
        double safeEmi         = req.getMonthlyIncome() * 0.50;
        double monthlySaving   = Math.max(0, totalEmi - safeEmi) + Math.max(0, monthlyInterest * 0.5);

        int projectedScore = Math.min(900, req.getCreditScore()
                + (utilization < 30 ? 30 : utilization < 50 ? 10 : -20)
                + (emiBurden < 40 ? 20 : emiBurden < 50 ? 0 : -30)
                + (req.getLatePaymentsLastYear() == 0 ? 20 : -req.getLatePaymentsLastYear() * 10));

        return FinancialHealthMetrics.builder()
                .creditUtilizationPct(utilization)
                .recommendedUtilizationPct(30.0)
                .debtToIncomeRatio(dti)
                .safeDebtToIncomeRatio(40.0)
                .emiBurdenRatio(emiBurden)
                .safeEmiBurdenRatio(50.0)
                .estimatedMonthlyInterest(monthlyInterest)
                .estimatedAnnualInterest(annualInterest)
                .interestIfMinimumOnly(interestIfMinOnly)
                .grossMonthlyCashFlow(grossCashFlow)
                .netFreeCashFlow(netFreeCashFlow)
                .currentMonthlyDebtPayment(totalEmi)
                .recommendedMonthlyDebtPayment(safeEmi)
                .monthlySavingIfOptimized(monthlySaving)
                .currentCreditScore(req.getCreditScore())
                .projectedScoreIfCorrected(projectedScore)
                .isUtilizationHealthy(utilization <= 30)
                .isDtiHealthy(dti <= 40)
                .isEmiBurdenHealthy(emiBurden <= 50)
                .isCashFlowPositive(netFreeCashFlow > 0)
                .build();
    }

    private CreditProfile buildProfile(CreditRequest req) {
        return CreditProfile.builder()
                .userId(req.getUserId())
                .monthlyIncome(req.getMonthlyIncome())
                .monthlyExpenses(req.getMonthlyExpenses())
                .totalCreditLimit(req.getTotalCreditLimit())
                .totalOutstandingBalance(req.getTotalOutstandingBalance())
                .numberOfCards(req.getNumberOfCards())
                .creditScore(req.getCreditScore())
                .paysMinimumOnly(req.isPaysMinimumOnly())
                .latePaymentsLastYear(req.getLatePaymentsLastYear())
                .missedPaymentsLastYear(req.getMissedPaymentsLastYear())
                .totalEmiPerMonth(req.getTotalEmiPerMonth())
                .numberOfActiveEmis(req.getNumberOfActiveEmis())
                .cashAdvanceAmount(req.getCashAdvanceAmount())
                .cashAdvanceFrequency(req.getCashAdvanceFrequency())
                .annualInterestRate(req.getAnnualInterestRate())
                .latePamentFee(req.getLatePamentFee())
                .otherLoanEmi(req.getOtherLoanEmi())
                .createdAt(LocalDateTime.now())
                .build();
    }

    private CreditResponse buildResponse(Long profileId, CreditRequest req,
                                         List<TrapResult> allTraps,
                                         RiskClassificationService.RiskResult risk,
                                         FinancialHealthMetrics metrics,
                                         String aiText, List<String> aiTips) {
        List<TrapResult> detected = allTraps.stream().filter(TrapResult::isDetected).toList();
        String urgency = switch (risk.riskLevel()) {
            case "HIGH"   -> "Immediate Action Required";
            case "MEDIUM" -> "Monitor & Improve";
            default       -> "On Track";
        };

        return CreditResponse.builder()
                .profileId(profileId)
                .userId(req.getUserId())
                .riskLevel(risk.riskLevel())
                .riskScore(risk.riskScore())
                .riskCategory(risk.riskCategory())
                .riskReasoning(risk.reasoning())
                .trapsDetectedCount(detected.size())
                .totalTrapsChecked(6)
                .detectedTraps(detected)
                .allTrapResults(allTraps)
                .healthMetrics(metrics)
                .aiRecommendation(aiText)
                .aiTips(aiTips)
                .urgencyLevel(urgency)
                .analysisTimestamp(LocalDateTime.now())
                .disclaimer("This analysis is for financial awareness purposes only and does not constitute " +
                        "financial advice. Consult a certified financial planner for personalized guidance.")
                .build();
    }

    private void publishAsync(Long profileId, CreditRequest req, List<TrapResult> traps,
                              RiskClassificationService.RiskResult risk,
                              FinancialHealthMetrics metrics, String aiText, List<String> aiTips) {
        try {
            String trapsJson    = objectMapper.writeValueAsString(traps);
            String aiTipsJson   = objectMapper.writeValueAsString(aiTips);
            CreditAnalysisMessage msg = CreditAnalysisMessage.builder()
                    .profileId(profileId)
                    .userId(req.getUserId())
                    .riskLevel(risk.riskLevel())
                    .riskScore(risk.riskScore())
                    .riskReasoning(risk.reasoning())
                    .trapsDetectedCount((int) traps.stream().filter(TrapResult::isDetected).count())
                    .trapsJson(trapsJson)
                    .creditUtilizationPct(metrics.getCreditUtilizationPct())
                    .debtToIncomeRatio(metrics.getDebtToIncomeRatio())
                    .emiBurdenRatio(metrics.getEmiBurdenRatio())
                    .estimatedAnnualInterest(metrics.getEstimatedAnnualInterest())
                    .freeCashFlow(metrics.getNetFreeCashFlow())
                    .aiRecommendation(aiText)
                    .aiTipsJson(aiTipsJson)
                    .build();
            producer.publishAnalysis(msg);
        } catch (Exception e) {
            log.error("Failed to publish to RabbitMQ for profile {}: {}", profileId, e.getMessage());
        }
    }

    private List<String> extractTips(String aiText) {
        return Arrays.stream(aiText.split("\n"))
                .filter(line -> line.startsWith("•") || line.startsWith("-") || line.matches("^\\d+\\..*"))
                .map(line -> line.replaceAll("^[•\\-\\d\\.\\s]+", "").trim())
                .filter(s -> s.length() > 10)
                .limit(8)
                .collect(Collectors.toList());
    }

    private List<TrapResult> parseTrapJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<TrapResult>>() {});
        } catch (Exception e) {
            log.warn("Could not parse traps JSON: {}", e.getMessage());
            return List.of();
        }
    }

    private List<String> parseAiTipsJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private String getRiskCategory(String riskLevel) {
        return switch (riskLevel) {
            case "LOW"  -> "Financially Stable";
            case "HIGH" -> "Danger Zone — Immediate Action";
            default     -> "Financial Monitor Required";
        };
    }
}
