package com.creditcard.credit_card_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("credit_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditProfile {

    @Id
    private Long id;

    @Column("user_id")
    private String userId;

    // Income
    @Column("monthly_income")
    private double monthlyIncome;

    @Column("monthly_expenses")
    private double monthlyExpenses;

    // Credit Card Details
    @Column("total_credit_limit")
    private double totalCreditLimit;

    @Column("total_outstanding_balance")
    private double totalOutstandingBalance;

    @Column("number_of_cards")
    private int numberOfCards;

    @Column("credit_score")
    private int creditScore;

    // Payment Behavior
    @Column("pays_minimum_only")
    private boolean paysMinimumOnly;

    @Column("late_payments_last_year")
    private int latePaymentsLastYear;

    @Column("missed_payments_last_year")
    private int missedPaymentsLastYear;

    // EMI Details
    @Column("total_emi_per_month")
    private double totalEmiPerMonth;

    @Column("number_of_active_emis")
    private int numberOfActiveEmis;

    // Cash Advance
    @Column("cash_advance_amount")
    private double cashAdvanceAmount;

    @Column("cash_advance_frequency")
    private int cashAdvanceFrequency;

    // Interest & Charges
    @Column("annual_interest_rate")
    private double annualInterestRate;

    @Column("late_payment_fee")
    private double latePamentFee;

    // Other Loans
    @Column("other_loan_emi")
    private double otherLoanEmi;

    @Column("created_at")
    private LocalDateTime createdAt;
}
