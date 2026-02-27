package com.creditcard.credit_card_service.exeception;

import lombok.extern.slf4j.Slf4j;
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
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleValidationErrors(WebExchangeBindException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, fe ->
                        fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value"));

        return Mono.just(ResponseEntity.badRequest().body(errorBody(
                "VALIDATION_ERROR", "Request validation failed", fieldErrors)));
    }

    @ExceptionHandler(CreditAnalysisException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleAnalysisException(CreditAnalysisException ex) {
        log.error("Credit analysis error: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorBody("ANALYSIS_ERROR", ex.getMessage(), null)));
    }

    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleRuntimeException(RuntimeException ex) {
        String msg = ex.getMessage();
        HttpStatus status = msg != null && msg.contains("not found")
                ? HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
        log.error("Runtime error: {}", msg);
        return Mono.just(ResponseEntity.status(status)
                .body(errorBody("RUNTIME_ERROR", msg, null)));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorBody("INTERNAL_ERROR", "An unexpected error occurred", null)));
    }

    private Map<String, Object> errorBody(String code, String message, Object details) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", code);
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now().toString());
        if (details != null) body.put("details", details);
        return body;
    }
}
