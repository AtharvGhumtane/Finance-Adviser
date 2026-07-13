package com.alex.ai_serviceAlex.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentProfileDTO {

    private UUID userId;

    @NotNull(message = "Annual income is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Annual income must be positive")
    private BigDecimal annualIncome;

    @NotNull(message = "Risk tolerance is required")
    @Min(value = 1, message = "Risk tolerance must be between 1 and 10")
    @Max(value = 10, message = "Risk tolerance must be between 1 and 10")
    private Integer riskTolerance;

    @NotNull(message = "Investment horizon is required")
    private InvestmentHorizon investmentHorizon;

    @NotBlank(message = "Target cryptocurrency is required")
    @Pattern(regexp = "^[A-Z]{3,10}$", message = "Invalid cryptocurrency symbol format")
    private String targetCryptocurrency;

    // Optional: Additional context
    private String additionalContext;

    public enum InvestmentHorizon {
        SHORT_TERM,  // < 1 year
        MEDIUM_TERM, // 1-3 years
        LONG_TERM    // > 3 years
    }
}
