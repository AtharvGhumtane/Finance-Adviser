package com.alex.ai_serviceAlex.controller;

import com.alex.ai_serviceAlex.dto.InvestmentProfileDTO;
import com.alex.ai_serviceAlex.dto.RecommendationResponseDTO;
import com.alex.ai_serviceAlex.messaging.RecommendationProducer;
import com.alex.ai_serviceAlex.service.RecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final RecommendationProducer recommendationProducer;

    /**
     * Generate recommendation ASYNCHRONOUSLY via RabbitMQ (NEW!)
     * POST /api/v1/recommendations/generate-async
     */
    @PostMapping("/generate-async")
    public Mono<ResponseEntity<String>> generateRecommendationAsync(
            @Valid @RequestBody InvestmentProfileDTO profile,
            @RequestHeader("X-User-ID") UUID userId) {

        log.info("📨 Received ASYNC recommendation request for user: {}", userId);
        profile.setUserId(userId);

        return Mono.fromRunnable(() -> recommendationProducer.sendRecommendationRequest(profile))
                .then(Mono.just(ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body("✅ Recommendation request queued! Check /history for results.")))
                .doOnError(error -> log.error("Error queueing recommendation for user: {}",
                        userId, error));
    }

    /**
     * Generate a new investment recommendation (Synchronous - Direct call, no RabbitMQ)
     * POST /api/v1/recommendations/generate
     */
    @PostMapping("/generate")
    public Mono<ResponseEntity<RecommendationResponseDTO>> generateRecommendation(
            @Valid @RequestBody InvestmentProfileDTO profile,
            @RequestHeader("X-User-ID") UUID userId) {

        log.info("Received sync recommendation request for user: {}", userId);
        profile.setUserId(userId);

        return recommendationService.processRecommendation(profile)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .doOnError(error -> log.error("Error generating recommendation for user: {}",
                        userId, error));
    }

    /**
     * Get user's recommendation history
     * GET /api/v1/recommendations/history
     */
    @GetMapping("/history")
    public Flux<RecommendationResponseDTO> getUserHistory(
            @RequestHeader("X-User-ID") UUID userId) {

        log.info("Fetching recommendation history for user: {}", userId);
        return recommendationService.getUserRecommendations(userId);
    }

    /**
     * Get recommendations by cryptocurrency
     * GET /api/v1/recommendations/history/{cryptocurrency}
     */
    @GetMapping("/history/{cryptocurrency}")
    public Flux<RecommendationResponseDTO> getHistoryByCrypto(
            @RequestHeader("X-User-ID") UUID userId,
            @PathVariable String cryptocurrency) {

        log.info("Fetching recommendations for user: {} and crypto: {}", userId, cryptocurrency);
        return recommendationService.getUserRecommendationsByCrypto(userId, cryptocurrency);
    }

    /**
     * Get recent recommendations (last N days)
     * GET /api/v1/recommendations/recent?days=30
     */
    @GetMapping("/recent")
    public Flux<RecommendationResponseDTO> getRecentRecommendations(
            @RequestHeader("X-User-ID") UUID userId,
            @RequestParam(defaultValue = "30") int days) {

        log.info("Fetching recent recommendations for user: {} (last {} days)", userId, days);
        return recommendationService.getRecentRecommendations(userId, days);
    }

    /**
     * Get specific recommendation by ID
     * GET /api/v1/recommendations/{id}
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<RecommendationResponseDTO>> getRecommendationById(
            @PathVariable Long id,
            @RequestHeader("X-User-ID") UUID userId) {

        log.info("Fetching recommendation {} for user: {}", id, userId);

        return recommendationService.getRecommendationById(id)
                .filter(rec -> rec.getUserId().equals(userId)) // Security: ensure user owns it
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    /**
     * Health check endpoint
     * GET /api/v1/recommendations/health
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<String>> healthCheck() {
        return Mono.just(ResponseEntity.ok("AI Recommendation Service is running"));
    }
}