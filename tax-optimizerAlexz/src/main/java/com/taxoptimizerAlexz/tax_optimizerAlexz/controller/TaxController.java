package com.taxoptimizerAlexz.tax_optimizerAlexz.controller;

import com.taxoptimizerAlexz.tax_optimizerAlexz.dto.*;
import com.taxoptimizerAlexz.tax_optimizerAlexz.exception.ProfileNotFoundException;
import com.taxoptimizerAlexz.tax_optimizerAlexz.model.TaxProfile;
import com.taxoptimizerAlexz.tax_optimizerAlexz.model.TaxRecommendation;
import com.taxoptimizerAlexz.tax_optimizerAlexz.repo.TaxProfileRepository;
import com.taxoptimizerAlexz.tax_optimizerAlexz.repo.TaxRecommendationRepository;
import com.taxoptimizerAlexz.tax_optimizerAlexz.service.TaxCalculationService;
import com.taxoptimizerAlexz.tax_optimizerAlexz.service.TaxOptimizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Reactive REST Controller — all endpoints return Mono/Flux.
 *
 * Base URL: /api/v1/tax
 *
 * POST  /optimize                     → Full optimization + Gemini AI + RabbitMQ
 * POST  /compare-regimes              → Quick Old vs New regime comparison (no AI)
 * POST  /hra-exemption                → HRA exemption calculator
 * GET   /profiles/{id}                → Get saved tax profile by ID
 * GET   /profiles/user/{userId}       → All profiles for a user
 * DELETE /profiles/{id}               → Delete a profile
 * GET   /recommendations/profile/{id} → Get AI recommendation for a profile
 * GET   /recommendations/user/{uid}   → All recommendations for a user
 * GET   /health                       → Health check
 */
@RestController
@RequestMapping("/api/v1/tax")
@RequiredArgsConstructor
public class TaxController {

    private final TaxOptimizationService      optimizationService;
    private final TaxCalculationService       calculationService;
    private final TaxProfileRepository        profileRepository;
    private final TaxRecommendationRepository recommendationRepository;

    // ─── 1. Full Optimization ─────────────────────────────────────────────────

    /**
     * POST /api/v1/tax/optimize
     *
     * Full pipeline: rule engine + Gemini AI + RabbitMQ publish + DB save.
     * Returns the complete TaxResponse. Recommendation persisted async via consumer.
     */
    @PostMapping("/optimize")
    public Mono<ResponseEntity<TaxResponse>> optimize(@Valid @RequestBody TaxRequest request) {
        return optimizationService.processAndOptimize(request)
                .map(ResponseEntity::ok);
    }

    // ─── 2. Quick Regime Comparison ───────────────────────────────────────────

    /**
     * POST /api/v1/tax/compare-regimes
     *
     * Lightweight — computes Old vs New regime, no Gemini, no DB, no RabbitMQ.
     */
    @PostMapping("/compare-regimes")
    public Mono<ResponseEntity<Map<String, Object>>> compareRegimes(
            @Valid @RequestBody TaxRequest request) {

        RegimeResult old = calculationService.calculateOldRegime(request);
        RegimeResult nw  = calculationService.calculateNewRegime(request);
        String recommended = old.getTotalTax() <= nw.getTotalTax() ? "Old Regime" : "New Regime";
        double saving = Math.abs(old.getTotalTax() - nw.getTotalTax());

        return Mono.just(ResponseEntity.ok(Map.of(
                "oldRegime",         old,
                "newRegime",         nw,
                "recommendedRegime", recommended,
                "taxSavings",        saving,
                "message", String.format("%s is better by ₹%.0f for your profile.", recommended, saving)
        )));
    }

    // ─── 3. HRA Exemption Calculator ─────────────────────────────────────────

    /**
     * POST /api/v1/tax/hra-exemption
     *
     * Calculates HRA exemption using "Least of 3" rule.
     * Fields needed: basicSalary, hra, rentPaid, cityType
     */
    @PostMapping("/hra-exemption")
    public Mono<ResponseEntity<Map<String, Object>>> hraExemption(@RequestBody TaxRequest request) {
        double exemption = calculationService.calculateHraExemption(request);
        double taxable   = request.getHra() - exemption;

        double rule2 = request.getRentPaid() - (0.10 * request.getBasicSalary());
        double rule3 = "METRO".equalsIgnoreCase(request.getCityType())
                ? 0.50 * request.getBasicSalary()
                : 0.40 * request.getBasicSalary();

        return Mono.just(ResponseEntity.ok(Map.of(
                "hraReceived",    request.getHra(),
                "hraExemption",   exemption,
                "taxableHra",     taxable,
                "rule1_actualHra", request.getHra(),
                "rule2_rentMinus10pct", rule2,
                "rule3_percentageOfBasic", rule3,
                "cityType",       request.getCityType(),
                "explanation", String.format(
                        "Exemption = MIN(₹%.0f, ₹%.0f, ₹%.0f) = ₹%.0f",
                        request.getHra(), rule2, rule3, exemption)
        )));
    }

    // ─── 4. Profile Endpoints ─────────────────────────────────────────────────

    @GetMapping("/profiles/{id}")
    public Mono<ResponseEntity<TaxProfile>> getProfile(@PathVariable Long id) {
        return profileRepository.findById(id)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.error(new ProfileNotFoundException(id)));
    }

    @GetMapping("/profiles/user/{userId}")
    public Flux<TaxProfile> getProfilesByUser(@PathVariable String userId) {
        return profileRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @DeleteMapping("/profiles/{id}")
    public Mono<ResponseEntity<Map<String, String>>> deleteProfile(@PathVariable Long id) {
        return profileRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProfileNotFoundException(id)))
                .flatMap(profile -> profileRepository.deleteById(id)
                        .then(Mono.just(ResponseEntity.ok(
                                Map.of("message", "Profile " + id + " deleted successfully.")
                        ))));
    }

    // ─── 5. Recommendation Endpoints ─────────────────────────────────────────

    /**
     * GET /api/v1/tax/recommendations/profile/{profileId}
     *
     * Fetch the AI recommendation saved for a specific profile.
     * Note: This is saved asynchronously — may not be available immediately after /optimize.
     */
    @GetMapping("/recommendations/profile/{profileId}")
    public Mono<ResponseEntity<TaxRecommendation>> getRecommendationByProfile(
            @PathVariable Long profileId) {
        return recommendationRepository.findByProfileId(profileId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.ACCEPTED)
                        .build()); // 202 if still being processed by consumer
    }

    /**
     * GET /api/v1/tax/recommendations/user/{userId}
     *
     * Fetch all AI recommendations for a user, latest first.
     */
    @GetMapping("/recommendations/user/{userId}")
    public Flux<TaxRecommendation> getRecommendationsByUser(@PathVariable String userId) {
        return recommendationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * PATCH /api/v1/tax/recommendations/{id}/status
     *
     * Update recommendation status (e.g. VIEWED, APPLIED).
     */
    @PatchMapping("/recommendations/{id}/status")
    public Mono<ResponseEntity<Map<String, String>>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return recommendationRepository.updateStatus(id, status.toUpperCase())
                .then(Mono.just(ResponseEntity.ok(
                        Map.of("message", "Status updated to " + status.toUpperCase())
                )));
    }

    // ─── 6. Health ────────────────────────────────────────────────────────────

    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, String>>> health() {
        return Mono.just(ResponseEntity.ok(Map.of(
                "status",  "UP",
                "service", "tax-optimizer-service",
                "fy",      "2024-25",
                "version", "1.0.0"
        )));
    }
}
