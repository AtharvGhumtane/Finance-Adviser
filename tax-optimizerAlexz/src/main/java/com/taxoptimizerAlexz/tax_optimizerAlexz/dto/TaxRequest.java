package com.taxoptimizerAlexz.tax_optimizerAlexz.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Incoming request DTO for tax optimization.
 * All monetary values in INR (annual).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxRequest {

    private String userId;

    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 100, message = "Age seems invalid")
    private int age;

    @Min(0)
    private int dependents;

    // ─── Income ───────────────────────────────────────────────────────────────
    @NotNull(message = "Gross salary is required")
    @Min(value = 0, message = "Gross salary cannot be negative")
    private double grossSalary;

    @Min(0) private double basicSalary;
    @Min(0) private double hra;
    @Min(0) private double da;
    @Min(0) private double specialAllowance;
    @Min(0) private double otherIncome;

    // ─── HRA / Rent ───────────────────────────────────────────────────────────
    @Min(0) private double rentPaid;
    private String cityType = "NON_METRO";   // "METRO" | "NON_METRO"

    // ─── Deductions ───────────────────────────────────────────────────────────
    @Min(0) @Max(150000) private double section80C;
    @Min(0)              private double section80D;
    @Min(0)              private double section80DParents;
    @Min(0) @Max(50000)  private double section80CCD1B;
    @Min(0)              private double section80EEA;
    @Min(0)              private double section80G;
    @Min(0)              private double section80TTA;
    @Min(0)              private double homeLoanInterest;
    @Min(0)              private double homeLoanPrincipal;

    // ─── Preferences ──────────────────────────────────────────────────────────
    private String riskAppetite  = "MEDIUM";   // LOW | MEDIUM | HIGH
    private String liquidityNeed = "MEDIUM";   // LOW | MEDIUM | HIGH
}
