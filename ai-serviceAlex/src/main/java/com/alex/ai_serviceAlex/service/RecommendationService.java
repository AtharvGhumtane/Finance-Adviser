package com.alex.ai_serviceAlex.service;

import com.alex.ai_serviceAlex.dto.InvestmentProfileDTO;
import com.alex.ai_serviceAlex.dto.RecommendationResponseDTO;
import com.alex.ai_serviceAlex.model.RecommendationEntity;
import com.alex.ai_serviceAlex.repo.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final GeminiAIService geminiAIService;
    private final RecommendationRepository repository;
    private static final String AI_MODEL_VERSION = "gemini-pro-v1";

    /**
     * Process investment profile and generate recommendation
     */
    public Mono<RecommendationResponseDTO> processRecommendation(InvestmentProfileDTO profile) {
        log.info("Processing recommendation request for user: {}", profile.getUserId());
        long startTime = System.currentTimeMillis();

        return geminiAIService.generateRecommendation(profile)
                .flatMap(aiResponse -> {
                    long processingTime = System.currentTimeMillis() - startTime;

                    // Parse AI response into structured components
                    ParsedRecommendation parsed = parseAIResponse(aiResponse);

                    // Build entity for persistence
                    RecommendationEntity entity = RecommendationEntity.builder()
                            .userId(profile.getUserId())
                            .targetCryptocurrency(profile.getTargetCryptocurrency())
                            .annualIncome(profile.getAnnualIncome())
                            .riskTolerance(profile.getRiskTolerance())
                            .investmentHorizon(profile.getInvestmentHorizon().name())
                            .recommendationText(aiResponse)
                            .confidenceScore(calculateConfidenceScore(profile))
                            .riskAssessment(parsed.riskAssessment())
                            .createdAt(LocalDateTime.now())
                            .processingTimeMs(processingTime)
                            .aiModelVersion(AI_MODEL_VERSION)
                            .build();

                    // Save to database
                    return repository.save(entity)
                            .map(saved -> mapToResponseDTO(saved, parsed))
                            .doOnSuccess(response -> log.info(
                                    "Successfully saved recommendation {} for user: {}",
                                    response.getRecommendationId(),
                                    profile.getUserId()));
                })
                .doOnError(error -> log.error("Failed to process recommendation for user: {}",
                        profile.getUserId(), error));
    }

    /**
     * Retrieve user's recommendation history
     */
    public Flux<RecommendationResponseDTO> getUserRecommendations(UUID userId) {
        log.info("Retrieving recommendations for user: {}", userId);

        return repository.findByUserIdOrderByCreatedAtDesc(userId)
                .map(entity -> mapToResponseDTO(entity, null))
                .doOnComplete(() -> log.info("Retrieved recommendations for user: {}", userId));
    }

    /**
     * Get recommendations by user and cryptocurrency
     */
    public Flux<RecommendationResponseDTO> getUserRecommendationsByCrypto(
            UUID userId, String cryptocurrency) {
        log.info("Retrieving recommendations for user: {} and crypto: {}", userId, cryptocurrency);

        return repository.findByUserIdAndTargetCryptocurrency(userId, cryptocurrency.toUpperCase())
                .map(entity -> mapToResponseDTO(entity, null));
    }

    /**
     * Get recent recommendations (last 30 days)
     */
    public Flux<RecommendationResponseDTO> getRecentRecommendations(UUID userId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);

        return repository.findRecentByUserId(userId, since)
                .map(entity -> mapToResponseDTO(entity, null));
    }

    /**
     * Get specific recommendation by ID
     */
    public Mono<RecommendationResponseDTO> getRecommendationById(Long id) {
        return repository.findById(id)
                .map(entity -> mapToResponseDTO(entity, null))
                .switchIfEmpty(Mono.error(new RuntimeException("Recommendation not found")));
    }

    /**
     * Calculate confidence score based on profile completeness and risk alignment
     */
    private Double calculateConfidenceScore(InvestmentProfileDTO profile) {
        double score = 0.7; // Base score

        // Adjust based on risk tolerance alignment
        if (profile.getRiskTolerance() >= 7 &&
                profile.getInvestmentHorizon() == InvestmentProfileDTO.InvestmentHorizon.SHORT_TERM) {
            score -= 0.1; // High risk + short term = lower confidence
        }

        if (profile.getRiskTolerance() <= 3 &&
                profile.getInvestmentHorizon() == InvestmentProfileDTO.InvestmentHorizon.LONG_TERM) {
            score += 0.15; // Conservative + long term = higher confidence
        }

        // Additional context improves confidence
        if (profile.getAdditionalContext() != null && !profile.getAdditionalContext().isEmpty()) {
            score += 0.1;
        }

        return Math.min(0.95, Math.max(0.5, score)); // Clamp between 0.5 and 0.95
    }

    /**
     * Parse AI response into structured components
     */
    private ParsedRecommendation parseAIResponse(String aiResponse) {
        String riskAssessment = extractSection(aiResponse, "RISK ASSESSMENT");
        String allocationSuggestion = extractSection(aiResponse, "PORTFOLIO ALLOCATION SUGGESTION");
        String keyInsights = extractSection(aiResponse, "KEY INSIGHTS");

        return new ParsedRecommendation(
                riskAssessment != null ? riskAssessment : "See full recommendation",
                allocationSuggestion != null ? allocationSuggestion : "See full recommendation",
                keyInsights != null ? keyInsights : "See full recommendation"
        );
    }

    /**
     * Extract section from AI response
     */
    private String extractSection(String text, String sectionHeader) {
        try {
            Pattern pattern = Pattern.compile(
                    sectionHeader + "[:\\s]*([\\s\\S]*?)(?=\\n\\n[A-Z]|$)",
                    Pattern.CASE_INSENSITIVE
            );
            Matcher matcher = pattern.matcher(text);

            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        } catch (Exception e) {
            log.warn("Could not extract section: {}", sectionHeader, e);
        }
        return null;
    }

    /**
     * Map entity to response DTO
     */
    private RecommendationResponseDTO mapToResponseDTO(
            RecommendationEntity entity,
            ParsedRecommendation parsed) {

        return RecommendationResponseDTO.builder()
                .recommendationId(entity.getId())
                .userId(entity.getUserId())
                .targetCryptocurrency(entity.getTargetCryptocurrency())
                .recommendationText(entity.getRecommendationText())
                .confidenceScore(entity.getConfidenceScore())
                .riskAssessment(entity.getRiskAssessment())
                .createdAt(entity.getCreatedAt())
                .processingTimeMs(entity.getProcessingTimeMs())
                .investmentStrategy(String.valueOf(entity.getInvestmentHorizon()))
                .timeframe(String.valueOf(entity.getInvestmentHorizon()))
                .keyInsights(parsed != null ? parsed.keyInsights() : null)
                .build();
    }

    /**
     * Internal record for parsed recommendation components
     */
    private record ParsedRecommendation(
            String riskAssessment,
            String allocationSuggestion,
            String keyInsights
    ) {}
}
