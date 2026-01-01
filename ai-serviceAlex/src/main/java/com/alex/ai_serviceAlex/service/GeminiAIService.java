package com.alex.ai_serviceAlex.service;

import com.alex.ai_serviceAlex.dto.InvestmentProfileDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.common.net.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeminiAIService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final int maxRetries;

    public GeminiAIService(
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.api.url}") String apiUrl,
            @Value("${gemini.api.timeout:30000}") int timeout,
            @Value("${gemini.api.max-retries:3}") int maxRetries) {

        this.webClient = webClientBuilder
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.maxRetries = maxRetries;
    }

    /**
     * Generate cryptocurrency investment recommendation using Gemini AI
     */
    public Mono<String> generateRecommendation(InvestmentProfileDTO profile) {
        log.info("Generating AI recommendation for user: {}, crypto: {}",
                profile.getUserId(), profile.getTargetCryptocurrency());

        String prompt = buildPrompt(profile);
        Map<String, Object> requestBody = buildGeminiRequest(prompt);

        return webClient.post()
                .uri(uriBuilder -> uriBuilder.queryParam("key", apiKey).build())
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::extractTextFromResponse)
                .retryWhen(Retry.backoff(maxRetries, Duration.ofSeconds(2))
                        .filter(throwable -> !(throwable instanceof IllegalArgumentException))
                        .doBeforeRetry(signal -> log.warn("Retrying Gemini API call, attempt: {}",
                                signal.totalRetries() + 1)))
                .doOnSuccess(response -> log.info("Successfully generated recommendation for user: {}",
                        profile.getUserId()))
                .doOnError(error -> log.error("Failed to generate recommendation for user: {}",
                        profile.getUserId(), error))
                .timeout(Duration.ofSeconds(30));
    }

    /**
     * Build comprehensive prompt for Gemini AI
     */
    private String buildPrompt(InvestmentProfileDTO profile) {
        return String.format("""
                You are a professional cryptocurrency investment advisor. Analyze the following investor profile and provide a detailed, personalized investment recommendation.
                
                INVESTOR PROFILE:
                - Target Cryptocurrency: %s
                - Annual Income: $%s
                - Risk Tolerance: %d/10 (1=Very Conservative, 10=Very Aggressive)
                - Investment Horizon: %s
                %s
                
                Please provide a comprehensive analysis including:
                
                1. INVESTMENT RECOMMENDATION (2-3 paragraphs)
                   - Suitability assessment for this specific cryptocurrency
                   - Alignment with risk profile and investment horizon
                   - Market context and current trends
                
                2. RISK ASSESSMENT
                   - Specific risks associated with this cryptocurrency
                   - Volatility considerations
                   - Market and regulatory risks
                
                3. PORTFOLIO ALLOCATION SUGGESTION
                   - Recommended allocation percentage based on risk tolerance
                   - Diversification strategy
                   - Rebalancing recommendations
                
                4. KEY INSIGHTS & ACTION ITEMS
                   - 3-5 specific, actionable recommendations
                   - Entry and exit strategies
                   - Monitoring guidelines
                
                Format your response in clear sections with the headers above. Be specific, data-driven, and tailored to this exact profile.
                """,
                profile.getTargetCryptocurrency(),
                profile.getAnnualIncome(),
                profile.getRiskTolerance(),
                profile.getInvestmentHorizon().name().replace("_", " "),
                profile.getAdditionalContext() != null ?
                        "- Additional Context: " + profile.getAdditionalContext() : "");
    }

    /**
     * Build Gemini API request body
     */
    private Map<String, Object> buildGeminiRequest(String prompt) {
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(textPart));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(content));

        // Configure generation parameters
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("topK", 40);
        generationConfig.put("topP", 0.95);
        generationConfig.put("maxOutputTokens", 2048);
        requestBody.put("generationConfig", generationConfig);

        return requestBody;
    }

    /**
     * Extract text content from Gemini API response
     */
    private String extractTextFromResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");

            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");

                if (parts.isArray() && !parts.isEmpty()) {
                    String text = parts.get(0).path("text").asText();
                    if (text != null && !text.isEmpty()) {
                        return text;
                    }
                }
            }

            log.error("Unexpected Gemini API response format: {}", responseBody);
            throw new IllegalArgumentException("Invalid response format from Gemini API");

        } catch (Exception e) {
            log.error("Error parsing Gemini API response", e);
            throw new RuntimeException("Failed to parse AI response", e);
        }
    }
}
