package com.creditcard.credit_card_service.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Represents one detected credit card trap from the rule engine.
 *
 * Six trap types:
 *  1. MINIMUM_PAYMENT  – Only paying minimum due → perpetual debt spiral
 *  2. CREDIT_OVERUSE   – Utilization > 30% → credit score damage
 *  3. CASH_WITHDRAWAL  – Card cash advance → 3-5% fee + 36-42% interest
 *  4. EMI_OVERLOAD     – Too many EMIs → cash flow crunch
 *  5. LATE_PAYMENT     – Repeated delays → penalty + score damage
 *  6. UNSAFE_PRACTICES – High debt-to-income, low score, too many cards
 */
@Data
@Builder
public class TrapResult {

    private String trapType;              // e.g. "MINIMUM_PAYMENT"
    private String trapName;              // e.g. "Minimum Payment Trap"
    private boolean detected;

    private String severity;              // LOW | MEDIUM | HIGH | CRITICAL

    private String explanation;           // What is the trap
    private String consequence;           // Financial impact if ignored
    private String recommendation;        // Corrective action

    // Quantified impact
    private double currentValue;          // e.g. current utilization 78%
    private double safeThreshold;         // e.g. recommended 30%
    private double estimatedMonthlyCost;  // Extra cost caused by this trap (₹)
    private double potentialSaving;       // ₹ savings if corrected
}
