package com.creditcard.credit_card_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Incoming request payload for credit card trap analysis.
 * All financial values are in INR.
 */
@Data
public class CreditRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    // ─── Income ───────────────────────────────────────────────────────────────

    @NotNull @Min(value = 0, message = "Monthly income cannot be negative")
    private Double monthlyIncome;

    @NotNull @Min(0)
    private Double monthlyExpenses;

    // ─── Credit Card Details ──────────────────────────────────────────────────

    @NotNull @Min(0)
    private Double totalCreditLimit;       // Sum of all card limits

    @NotNull @Min(0)
    private Double totalOutstandingBalance; // Current total dues across all cards

    @Min(1) @Max(20)
    private int numberOfCards = 1;

    @NotNull @Min(300) @Max(900)
    private Integer creditScore;

    // ─── Payment Behavior ─────────────────────────────────────────────────────

    private boolean paysMinimumOnly = false; // Paying only minimum due each month?

    @Min(0) @Max(12)
    private int latePaymentsLastYear = 0;

    @Min(0) @Max(12)
    private int missedPaymentsLastYear = 0;

    // ─── EMI Details ──────────────────────────────────────────────────────────

    @Min(0)
    private double totalEmiPerMonth = 0;    // Credit card EMIs (converted purchases)

    @Min(0)
    private int numberOfActiveEmis = 0;

    // ─── Cash Advance ─────────────────────────────────────────────────────────

    @Min(0)
    private double cashAdvanceAmount = 0;   // Last month's cash withdrawal from card

    @Min(0)
    private int cashAdvanceFrequency = 0;   // Times per month

    // ─── Charges ──────────────────────────────────────────────────────────────

    @DecimalMin("0.0") @DecimalMax("60.0")
    private double annualInterestRate = 36.0;  // Bank's card APR

    @Min(0)
    private double latePamentFee = 0;

    // ─── Other Obligations ────────────────────────────────────────────────────

    @Min(0)
    private double otherLoanEmi = 0;        // Home loan, car loan, personal loan EMIs
}
