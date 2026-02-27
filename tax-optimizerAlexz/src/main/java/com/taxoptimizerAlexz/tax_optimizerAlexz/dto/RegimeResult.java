package com.taxoptimizerAlexz.tax_optimizerAlexz.dto;

import lombok.*;
import java.util.Map;

/**
 * Computed tax result for one regime (Old or New).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegimeResult {

    private String  regimeName;
    private double  grossIncome;
    private double  totalDeductions;
    private double  taxableIncome;
    private double  basicTax;
    private double  surcharge;
    private double  educationCess;
    private double  totalTax;
    private double  effectiveTaxRate;
    private double  inHandAnnual;
    private double  inHandMonthly;

    private Map<String, Double> deductionBreakdown;
    private Map<String, Double> slabBreakdown;
}
