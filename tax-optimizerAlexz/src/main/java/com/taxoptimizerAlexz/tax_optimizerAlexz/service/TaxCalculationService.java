package com.taxoptimizerAlexz.tax_optimizerAlexz.service;

import com.taxoptimizerAlexz.tax_optimizerAlexz.dto.RegimeResult;
import com.taxoptimizerAlexz.tax_optimizerAlexz.dto.TaxRequest;
import com.taxoptimizerAlexz.tax_optimizerAlexz.exception.TaxCalculationException;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Pure rule-based tax calculation engine for FY 2024-25 (AY 2025-26).
 * All methods are synchronous — no I/O, just math.
 *
 * Covers:
 *  - Old regime: slabs + all deductions (80C, 80D, 80CCD, HRA, 24b, etc.)
 *  - New regime: Budget 2024 revised slabs + ₹75,000 standard deduction
 *  - HRA: Least of 3 rule
 *  - Senior (60+) / Super Senior (80+) thresholds
 *  - Section 87A rebate, surcharge, 4% cess
 */
@Service
public class TaxCalculationService {

    // ─── Statutory Limits ─────────────────────────────────────────────────────
    private static final double STD_DEDUCTION_OLD  = 50_000;
    private static final double STD_DEDUCTION_NEW  = 75_000;
    private static final double LIMIT_80C          = 1_50_000;
    private static final double LIMIT_80CCD1B      = 50_000;
    private static final double LIMIT_24B          = 2_00_000;
    private static final double LIMIT_80TTA        = 10_000;
    private static final double LIMIT_80EEA        = 1_50_000;
    private static final double CESS_RATE          = 0.04;

    // ─── Public API ───────────────────────────────────────────────────────────

    public RegimeResult calculateOldRegime(TaxRequest req) {
        try {
            double gross = req.getGrossSalary() + req.getOtherIncome();

            double hraExemption  = calculateHraExemption(req);
            double ded80C        = Math.min(req.getSection80C() + req.getHomeLoanPrincipal(), LIMIT_80C);
            double ded80CCD      = Math.min(req.getSection80CCD1B(), LIMIT_80CCD1B);
            double ded80D        = calc80D(req.getSection80D(), req.getSection80DParents(), req.getAge());
            double ded24B        = Math.min(req.getHomeLoanInterest(), LIMIT_24B);
            double ded80EEA      = Math.min(req.getSection80EEA(), LIMIT_80EEA);
            double ded80G        = req.getSection80G();
            double ded80TTA      = Math.min(req.getSection80TTA(), LIMIT_80TTA);

            double totalDeductions = STD_DEDUCTION_OLD + hraExemption + ded80C
                    + ded80CCD + ded80D + ded24B + ded80EEA + ded80G + ded80TTA;

            double taxableIncome = Math.max(0, gross - totalDeductions);

            Map<String, Double> slabs   = oldRegimeSlabs(taxableIncome, req.getAge());
            double basicTax             = slabs.values().stream().mapToDouble(Double::doubleValue).sum();

            if (taxableIncome <= 5_00_000) basicTax = 0; // 87A rebate

            double surcharge = surcharge(basicTax, taxableIncome);
            double cess      = (basicTax + surcharge) * CESS_RATE;
            double totalTax  = basicTax + surcharge + cess;

            Map<String, Double> dedBreakdown = new LinkedHashMap<>();
            dedBreakdown.put("Standard Deduction",            STD_DEDUCTION_OLD);
            dedBreakdown.put("HRA Exemption",                 hraExemption);
            dedBreakdown.put("Section 80C",                   ded80C);
            dedBreakdown.put("Section 80CCD(1B) NPS",         ded80CCD);
            dedBreakdown.put("Section 80D Health Insurance",  ded80D);
            dedBreakdown.put("Section 24(b) Home Loan Int.",  ded24B);
            dedBreakdown.put("Section 80EEA",                 ded80EEA);
            dedBreakdown.put("Section 80G Donations",         ded80G);
            dedBreakdown.put("Section 80TTA Savings Int.",    ded80TTA);

            return buildResult("Old Regime", gross, totalDeductions, taxableIncome,
                    basicTax, surcharge, cess, totalTax, dedBreakdown, slabs);

        } catch (Exception e) {
            throw new TaxCalculationException("Failed to compute Old Regime tax: " + e.getMessage(), e);
        }
    }

    public RegimeResult calculateNewRegime(TaxRequest req) {
        try {
            double gross         = req.getGrossSalary() + req.getOtherIncome();
            double taxableIncome = Math.max(0, gross - STD_DEDUCTION_NEW);

            Map<String, Double> slabs = newRegimeSlabs(taxableIncome);
            double basicTax           = slabs.values().stream().mapToDouble(Double::doubleValue).sum();

            if (taxableIncome <= 7_00_000) basicTax = 0; // 87A rebate (new regime)

            double surcharge = surcharge(basicTax, taxableIncome);
            double cess      = (basicTax + surcharge) * CESS_RATE;
            double totalTax  = basicTax + surcharge + cess;

            Map<String, Double> dedBreakdown = new LinkedHashMap<>();
            dedBreakdown.put("Standard Deduction", STD_DEDUCTION_NEW);

            return buildResult("New Regime", gross, STD_DEDUCTION_NEW, taxableIncome,
                    basicTax, surcharge, cess, totalTax, dedBreakdown, slabs);

        } catch (Exception e) {
            throw new TaxCalculationException("Failed to compute New Regime tax: " + e.getMessage(), e);
        }
    }

    /**
     * HRA Exemption = MIN of:
     *  1. Actual HRA received
     *  2. Rent paid - 10% of basic salary
     *  3. 50% basic (Metro) / 40% basic (Non-Metro)
     */
    public double calculateHraExemption(TaxRequest req) {
        if (req.getRentPaid() <= 0 || req.getHra() <= 0) return 0;
        double rule1 = req.getHra();
        double rule2 = req.getRentPaid() - (0.10 * req.getBasicSalary());
        double rule3 = ("METRO".equalsIgnoreCase(req.getCityType()))
                ? 0.50 * req.getBasicSalary()
                : 0.40 * req.getBasicSalary();
        return Math.max(0, Math.min(rule1, Math.min(rule2, rule3)));
    }

    // ─── Private Helpers ──────────────────────────────────────────────────────

    private double calc80D(double self, double parents, int age) {
        double selfLimit    = age >= 60 ? 50_000 : 25_000;
        double parentsLimit = 50_000;
        return Math.min(self, selfLimit) + Math.min(parents, parentsLimit);
    }

    private Map<String, Double> oldRegimeSlabs(double income, int age) {
        Map<String, Double> s = new LinkedHashMap<>();
        if (age >= 80) {
            s.put("0–5L (0%)",    0.0);
            s.put("5L–10L (20%)", slab(income, 5_00_000, 10_00_000, 0.20));
            s.put("Above 10L (30%)", slab(income, 10_00_000, Double.MAX_VALUE, 0.30));
        } else if (age >= 60) {
            s.put("0–3L (0%)",    0.0);
            s.put("3L–5L (5%)",   slab(income, 3_00_000, 5_00_000, 0.05));
            s.put("5L–10L (20%)", slab(income, 5_00_000, 10_00_000, 0.20));
            s.put("Above 10L (30%)", slab(income, 10_00_000, Double.MAX_VALUE, 0.30));
        } else {
            s.put("0–2.5L (0%)",    0.0);
            s.put("2.5L–5L (5%)",   slab(income, 2_50_000, 5_00_000, 0.05));
            s.put("5L–10L (20%)",   slab(income, 5_00_000, 10_00_000, 0.20));
            s.put("Above 10L (30%)", slab(income, 10_00_000, Double.MAX_VALUE, 0.30));
        }
        return s;
    }

    private Map<String, Double> newRegimeSlabs(double income) {
        Map<String, Double> s = new LinkedHashMap<>();
        s.put("0–3L (0%)",       0.0);
        s.put("3L–7L (5%)",      slab(income, 3_00_000, 7_00_000, 0.05));
        s.put("7L–10L (10%)",    slab(income, 7_00_000, 10_00_000, 0.10));
        s.put("10L–12L (15%)",   slab(income, 10_00_000, 12_00_000, 0.15));
        s.put("12L–15L (20%)",   slab(income, 12_00_000, 15_00_000, 0.20));
        s.put("Above 15L (30%)", slab(income, 15_00_000, Double.MAX_VALUE, 0.30));
        return s;
    }

    private double slab(double income, double from, double to, double rate) {
        if (income <= from) return 0;
        return (Math.min(income, to) - from) * rate;
    }

    private double surcharge(double tax, double income) {
        if (income <= 50_00_000)    return 0;
        if (income <= 1_00_00_000)  return tax * 0.10;
        if (income <= 2_00_00_000)  return tax * 0.15;
        if (income <= 5_00_00_000)  return tax * 0.25;
        return tax * 0.37;
    }

    private RegimeResult buildResult(String name, double gross, double deductions,
                                     double taxable, double basic, double surcharge,
                                     double cess, double total,
                                     Map<String, Double> dedBreakdown,
                                     Map<String, Double> slabBreakdown) {
        double inHand = Math.max(0, gross - total);
        return RegimeResult.builder()
                .regimeName(name)
                .grossIncome(gross)
                .totalDeductions(deductions)
                .taxableIncome(taxable)
                .basicTax(basic)
                .surcharge(surcharge)
                .educationCess(cess)
                .totalTax(total)
                .effectiveTaxRate(gross > 0 ? (total / gross) * 100 : 0)
                .inHandAnnual(inHand)
                .inHandMonthly(inHand / 12)
                .deductionBreakdown(dedBreakdown)
                .slabBreakdown(slabBreakdown)
                .build();
    }
}
