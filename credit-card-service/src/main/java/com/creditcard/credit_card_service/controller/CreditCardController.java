package com.creditcard.credit_card_service.controller;

import com.creditcard.credit_card_service.dto.CreditRequest;
import com.creditcard.credit_card_service.dto.CreditResponse;
import com.creditcard.credit_card_service.dto.TrapResult;
import com.creditcard.credit_card_service.model.CreditAnalysis;
import com.creditcard.credit_card_service.model.CreditProfile;
import com.creditcard.credit_card_service.service.CreditCardService;
import com.creditcard.credit_card_service.service.RiskClassificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Reactive REST Controller for Credit Card Trap Awareness & Risk Analysis API.
 *
 * All endpoints return Mono<> or Flux<> — fully non-blocking.
 *
 * Base URL: /api/v1/credit
 *
 * Endpoints:
 *  POST   /analyze              → Full analysis: traps + ML risk + AI recommendations
 *  POST   /quick-trap-check     → Fast rule-based trap detection only (no DB, no AI)
 *  POST   /risk-only            → ML risk classification only
 *  GET    /profiles/{id}        → Fetch credit profile by ID
 *  GET    /profiles/user/{uid}  → All profiles for a user
 *  DELETE /profiles/{id}        → Delete a profile
 *  GET    /analyses/profile/{id}→ Get full analysis for a profile
 *  GET    /analyses/user/{uid}  → All analyses for a user
 *  PATCH  /analyses/{id}/status → Update analysis status
 *  GET    /health               → Service health check
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/credit")
@RequiredArgsConstructor
public class CreditCardController {

    private final CreditCardService creditCardService;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /analyze
    // Full pipeline: rule engine + ML + Gemini AI + DB persistence
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/analyze")
    public Mono<ResponseEntity<CreditResponse>> analyze(@Valid @RequestBody CreditRequest request) {
        log.info("Full analysis request received for user: {}", request.getUserId());
        return creditCardService.analyzeAndRecommend(request)
                .map(ResponseEntity::ok)
                .doOnError(e -> log.error("Analysis failed: {}", e.getMessage()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /quick-trap-check
    // Instant trap detection — no DB write, no AI call
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/quick-trap-check")
    public Mono<ResponseEntity<Map<String, Object>>> quickTrapCheck(@Valid @RequestBody CreditRequest request) {
        return creditCardService.quickTrapCheck(request)
                .map(traps -> {
                    long detected = traps.stream().filter(TrapResult::isDetected).count();
                    return ResponseEntity.ok(Map.of(
                            "userId", request.getUserId(),
                            "trapsChecked", 6,
                            "trapsDetected", detected,
                            "traps", traps,
                            "summary", detected == 0
                                    ? "No traps detected — excellent credit behavior!"
                                    : detected + " trap(s) detected. Consider a full analysis."
                    ));
                });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /risk-only
    // ML Decision Tree classification only — instant, no side effects
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/risk-only")
    public Mono<ResponseEntity<RiskClassificationService.RiskResult>> classifyRisk(
            @Valid @RequestBody CreditRequest request) {
        return creditCardService.classifyRiskOnly(request)
                .map(ResponseEntity::ok);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /profiles/{id}
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/profiles/{id}")
    public Mono<ResponseEntity<CreditProfile>> getProfile(@PathVariable Long id) {
        return creditCardService.getProfileById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /profiles/user/{userId}
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/profiles/user/{userId}")
    public Flux<CreditProfile> getProfilesByUser(@PathVariable String userId) {
        return creditCardService.getProfilesByUser(userId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /profiles/{id}
    // ─────────────────────────────────────────────────────────────────────────
    @DeleteMapping("/profiles/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteProfile(@PathVariable Long id) {
        return creditCardService.getProfileById(id)
                .flatMap(p -> Mono.empty()); // Soft-delete or extend as needed
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /analyses/profile/{profileId}
    // Returns 202 ACCEPTED if analysis not yet available (still being processed)
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/analyses/profile/{profileId}")
    public Mono<ResponseEntity<CreditResponse>> getAnalysis(@PathVariable Long profileId) {
        return creditCardService.getAnalysisByProfileId(profileId)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    if (e.getMessage() != null && e.getMessage().contains("not yet available")) {
                        return Mono.just(ResponseEntity.accepted().<CreditResponse>build());
                    }
                    return Mono.just(ResponseEntity.notFound().<CreditResponse>build());
                });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /analyses/user/{userId}
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/analyses/user/{userId}")
    public Flux<CreditAnalysis> getAnalysesByUser(@PathVariable String userId) {
        return creditCardService.getAnalysesByUser(userId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PATCH /analyses/{id}/status
    // ─────────────────────────────────────────────────────────────────────────
    @PatchMapping("/analyses/{id}/status")
    public Mono<ResponseEntity<Void>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        List<String> validStatuses = List.of("GENERATED", "VIEWED", "ACTION_TAKEN");
        if (!validStatuses.contains(status)) {
            return Mono.just(ResponseEntity.badRequest().<Void>build());
        }
        return creditCardService.updateStatus(id, status)
                .then(Mono.just(ResponseEntity.<Void>noContent().build()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /health
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> health() {
        return Mono.just(ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "credit-card-service",
                "port", 5058,
                "features", List.of(
                        "6-Trap Rule Engine",
                        "Decision Tree ML Risk Classifier",
                        "Gemini AI Explainability",
                        "Financial Health Metrics",
                        "Async RabbitMQ Persistence",
                        "Reactive WebFlux + R2DBC"
                ),
                "timestamp", LocalDateTime.now().toString()
        )));
    }
}
