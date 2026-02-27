package com.taxoptimizerAlexz.tax_optimizerAlexz.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * R2DBC entity — maps to 'tax_profiles' table in PostgreSQL.
 * Stores the taxpayer's full financial profile submitted for optimization.
 * No JPA annotations — pure R2DBC / Spring Data Relational.
 */
@Table("tax_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxProfile {

    @Id
    private Long id;

    // ─── Identity ────────────────────────────────────────────────────────────
    @Column("user_id")
    private String userId;

    @Column("age")
    private int age;

    @Column("dependents")
    private int dependents;

    // ─── Income ───────────────────────────────────────────────────────────────
    @Column("gross_salary")
    private double grossSalary;

    @Column("basic_salary")
    private double basicSalary;

    @Column("hra")
    private double hra;

    @Column("da")
    private double da;

    @Column("special_allowance")
    private double specialAllowance;

    @Column("other_income")
    private double otherIncome;

    // ─── HRA / Rent ───────────────────────────────────────────────────────────
    @Column("rent_paid")
    private double rentPaid;

    @Column("city_type")
    private String cityType;           // "METRO" | "NON_METRO"

    // ─── Deductions ───────────────────────────────────────────────────────────
    @Column("section_80c")
    private double section80C;

    @Column("section_80d")
    private double section80D;

    @Column("section_80d_parents")
    private double section80DParents;

    @Column("section_80ccd1b")
    private double section80CCD1B;

    @Column("section_80eea")
    private double section80EEA;

    @Column("section_80g")
    private double section80G;

    @Column("section_80tta")
    private double section80TTA;

    @Column("home_loan_interest")
    private double homeLoanInterest;

    @Column("home_loan_principal")
    private double homeLoanPrincipal;

    // ─── Preferences ──────────────────────────────────────────────────────────
    @Column("risk_appetite")
    private String riskAppetite;       // "LOW" | "MEDIUM" | "HIGH"

    @Column("liquidity_need")
    private String liquidityNeed;      // "LOW" | "MEDIUM" | "HIGH"

    // ─── Metadata ─────────────────────────────────────────────────────────────
    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;
}
