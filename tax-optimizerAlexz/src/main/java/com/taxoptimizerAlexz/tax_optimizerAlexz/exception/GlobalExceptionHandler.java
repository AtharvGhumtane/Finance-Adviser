package com.taxoptimizerAlexz.tax_optimizerAlexz.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for reactive WebFlux controllers.
 * Returns consistent error response structure across all endpoints.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ─── Validation Errors (@Valid) ───────────────────────────────────────────

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleValidation(WebExchangeBindException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError err : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(err.getField(), err.getDefaultMessage());
        }
        return Mono.just(ResponseEntity.badRequest().body(errorBody(
                400, "Validation Failed", "Check field errors", fieldErrors
        )));
    }

    // ─── Tax Calculation Error ────────────────────────────────────────────────

    @ExceptionHandler(TaxCalculationException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleTaxCalc(TaxCalculationException ex) {
        log.error("Tax calculation error: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorBody(
                422, "Tax Calculation Error", ex.getMessage(), null
        )));
    }

    // ─── Gemini AI Error ──────────────────────────────────────────────────────

    @ExceptionHandler(GeminiServiceException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleGemini(GeminiServiceException ex) {
        log.error("Gemini service error: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorBody(
                503, "AI Service Unavailable", ex.getMessage(), null
        )));
    }

    // ─── Profile Not Found ────────────────────────────────────────────────────

    @ExceptionHandler(ProfileNotFoundException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleNotFound(ProfileNotFoundException ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody(
                404, "Not Found", ex.getMessage(), null
        )));
    }

    // ─── Generic Fallback ─────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody(
                500, "Internal Server Error", ex.getMessage(), null
        )));
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private Map<String, Object> errorBody(int status, String error, String message, Object details) {
        Map<String, Object> body = new HashMap<>();
        body.put("status",    status);
        body.put("error",     error);
        body.put("message",   message);
        body.put("timestamp", LocalDateTime.now().toString());
        if (details != null) body.put("details", details);
        return body;
    }
}
