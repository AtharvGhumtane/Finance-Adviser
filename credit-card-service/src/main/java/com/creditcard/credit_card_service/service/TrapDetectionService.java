package com.creditcard.credit_card_service.service;

import com.creditcard.credit_card_service.dto.CreditRequest;
import com.creditcard.credit_card_service.dto.TrapResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Rule-based engine that detects 6 major credit card traps.
 *
 * Each rule encodes well-known banking and financial principles:
 *
 *  TRAP 1: Minimum Payment Trap  — only paying minimum due compounds debt indefinitely
 *  TRAP 2: Credit Overuse Trap   — utilization > 30% harms credit score; > 75% is critical
 *  TRAP 3: Cash Withdrawal Trap  — cash advance fees 2.5–3.5% + interest from Day 1, no grace
 *  TRAP 4: EMI Overload Trap     — total EMI > 50% of income → cash flow collapse
 *  TRAP 5: Late Payment Trap     — repeated late payments → penalties + score damage
 *  TRAP 6: Unsafe Practices Trap — high DTI, low score, too many cards
 */
@Service
public class TrapDetectionService {

    // Industry-standard thresholds
    private static final double UTILIZATION_WARNING      = 30.0;
    private static final double UTILIZATION_DANGER       = 75.0;
    private static final double EMI_BURDEN_LIMIT         = 50.0;
    private static final double EMI_BURDEN_CRITICAL      = 65.0;
    private static final double DTI_SAFE_LIMIT           = 40.0;
    private static final double DTI_DANGER_LIMIT         = 60.0;
    private static final int    LATE_PAYMENT_WARNING     = 1;
    private static final int    LATE_PAYMENT_HIGH_RISK   = 3;
    private static final int    CASH_ADVANCE_WARNING_FREQ= 1;
    private static final double MIN_PAYMENT_RATE         = 0.05; // 5% of outstanding

    public List<TrapResult> detectAllTraps(CreditRequest req) {
        List<TrapResult> results = new ArrayList<>();
        results.add(detectMinimumPaymentTrap(req));
        results.add(detectCreditOveruseTrap(req));
        results.add(detectCashWithdrawalTrap(req));
        results.add(detectEmiOverloadTrap(req));
        results.add(detectLatePaymentTrap(req));
        results.add(detectUnsafePracticesTrap(req));
        return results;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TRAP 1: Minimum Payment Trap
    // ─────────────────────────────────────────────────────────────────────────
    private TrapResult detectMinimumPaymentTrap(CreditRequest req) {
        boolean detected = req.isPaysMinimumOnly() && req.getTotalOutstandingBalance() > 0;

        double monthlyRate   = req.getAnnualInterestRate() / 1200.0;
        double balance       = req.getTotalOutstandingBalance();
        double minPayment    = balance * MIN_PAYMENT_RATE;
        double monthlyInterest = balance * monthlyRate;
        // Interest accrued per month when only paying minimum
        double extraInterestCost = detected ? monthlyInterest : 0;
        // Years to pay off at minimum payment
        double yearsToPayOff = (balance > 0 && monthlyRate > 0 && minPayment > monthlyInterest)
                ? Math.log(minPayment / (minPayment - monthlyInterest)) / Math.log(1 + monthlyRate) / 12
                : 99;

        String severity = detected ? (req.getAnnualInterestRate() > 36 ? "CRITICAL" : "HIGH") : "NONE";

        return TrapResult.builder()
                .trapType("MINIMUM_PAYMENT")
                .trapName("Minimum Payment Trap")
                .detected(detected)
                .severity(severity)
                .explanation("Paying only the minimum due (typically 5% of balance) means " +
                        "95% of your debt survives each month and compounds at " +
                        String.format("%.1f%%", req.getAnnualInterestRate()) + " APR.")
                .consequence(detected
                        ? String.format("At minimum payments only, it will take approximately %.1f years " +
                                "and cost ₹%.0f in additional interest to clear your ₹%.0f balance.",
                        yearsToPayOff,
                        balance * req.getAnnualInterestRate() / 100 * Math.max(yearsToPayOff, 1),
                        balance)
                        : "No minimum payment trap detected.")
                .recommendation("Always pay more than the minimum due. Aim to pay 100% of the statement " +
                        "balance each month. If not possible, pay at least 3–5× the minimum.")
                .currentValue(minPayment)
                .safeThreshold(balance) // Ideal: pay full balance
                .estimatedMonthlyCost(extraInterestCost)
                .potentialSaving(extraInterestCost * 12)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TRAP 2: Credit Overuse Trap
    // ─────────────────────────────────────────────────────────────────────────
    private TrapResult detectCreditOveruseTrap(CreditRequest req) {
        double utilization = req.getTotalCreditLimit() > 0
                ? (req.getTotalOutstandingBalance() / req.getTotalCreditLimit()) * 100.0
                : 0.0;

        boolean detected = utilization > UTILIZATION_WARNING;
        String severity;
        String consequence;

        if (utilization >= UTILIZATION_DANGER) {
            severity = "CRITICAL";
            consequence = String.format(
                    "Your credit utilization is %.1f%% — well above the 75%% danger zone. " +
                            "This can drop your credit score by 100–150 points, making future loans expensive or impossible.",
                    utilization);
        } else if (utilization >= 50) {
            severity = "HIGH";
            consequence = String.format(
                    "At %.1f%% utilization, your credit score is likely already declining. " +
                            "Lenders view you as a high-risk borrower.", utilization);
        } else if (utilization > UTILIZATION_WARNING) {
            severity = "MEDIUM";
            consequence = String.format(
                    "Utilization at %.1f%% is above the 30%% recommendation. " +
                            "This will mildly suppress your credit score.", utilization);
        } else {
            severity = "NONE";
            consequence = "Credit utilization is healthy at " + String.format("%.1f%%", utilization) + ".";
        }

        // Ideal balance = 30% of limit
        double idealBalance      = req.getTotalCreditLimit() * 0.30;
        double excessBalance     = Math.max(0, req.getTotalOutstandingBalance() - idealBalance);
        double monthlyCostOfExcess = excessBalance * (req.getAnnualInterestRate() / 1200.0);

        return TrapResult.builder()
                .trapType("CREDIT_OVERUSE")
                .trapName("Credit Overuse Trap")
                .detected(detected)
                .severity(severity)
                .explanation("Credit utilization ratio (outstanding / limit) is a major credit score factor. " +
                        "Bureaus like CIBIL penalize utilization above 30%. Banks use this to judge credit hunger.")
                .consequence(consequence)
                .recommendation(String.format(
                        "Reduce your outstanding balance from ₹%.0f to below ₹%.0f (30%% of ₹%.0f limit). " +
                                "Request a credit limit increase or split spending across cards to lower per-card utilization.",
                        req.getTotalOutstandingBalance(), idealBalance, req.getTotalCreditLimit()))
                .currentValue(utilization)
                .safeThreshold(UTILIZATION_WARNING)
                .estimatedMonthlyCost(monthlyCostOfExcess)
                .potentialSaving(monthlyCostOfExcess * 12)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TRAP 3: Cash Withdrawal Trap
    // ─────────────────────────────────────────────────────────────────────────
    private TrapResult detectCashWithdrawalTrap(CreditRequest req) {
        boolean detected = req.getCashAdvanceAmount() > 0 || req.getCashAdvanceFrequency() > 0;

        // Cash advance fee: 2.5% of amount, minimum ₹500
        double advanceFee       = Math.max(req.getCashAdvanceAmount() * 0.025, req.getCashAdvanceAmount() > 0 ? 500 : 0);
        double monthlyInterest  = req.getCashAdvanceAmount() * (req.getAnnualInterestRate() / 1200.0);
        double totalMonthlyCost = advanceFee + monthlyInterest; // No grace period on cash advances!

        String severity = !detected ? "NONE"
                : req.getCashAdvanceFrequency() >= 2 ? "CRITICAL"
                : req.getCashAdvanceAmount() > 50000 ? "HIGH" : "MEDIUM";

        return TrapResult.builder()
                .trapType("CASH_WITHDRAWAL")
                .trapName("Cash Withdrawal Trap")
                .detected(detected)
                .severity(severity)
                .explanation("Credit card cash advances are one of the most expensive forms of borrowing. " +
                        "A 2.5–3.5% transaction fee applies immediately, and interest (typically 3–3.5%/month) " +
                        "starts from Day 1 with NO grace period.")
                .consequence(detected
                        ? String.format("Your cash advance of ₹%.0f costs ₹%.0f in fees + ₹%.0f/month interest " +
                                "= ₹%.0f total cost in Month 1 alone. Over 12 months: ₹%.0f.",
                        req.getCashAdvanceAmount(), advanceFee, monthlyInterest,
                        totalMonthlyCost, advanceFee + monthlyInterest * 12)
                        : "No cash advance activity detected — good practice.")
                .recommendation("Never use credit cards for cash withdrawals. Use a personal loan or overdraft " +
                        "facility instead. If unavoidable, repay the cash advance within the same billing cycle.")
                .currentValue(req.getCashAdvanceAmount())
                .safeThreshold(0)
                .estimatedMonthlyCost(totalMonthlyCost)
                .potentialSaving(totalMonthlyCost * 12)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TRAP 4: EMI Overload Trap
    // ─────────────────────────────────────────────────────────────────────────
    private TrapResult detectEmiOverloadTrap(CreditRequest req) {
        double totalObligations = req.getTotalEmiPerMonth() + req.getOtherLoanEmi();
        double emiBurdenPct     = req.getMonthlyIncome() > 0
                ? (totalObligations / req.getMonthlyIncome()) * 100 : 0;

        boolean detected = emiBurdenPct > EMI_BURDEN_LIMIT;
        String severity  = !detected ? "NONE"
                : emiBurdenPct >= EMI_BURDEN_CRITICAL ? "CRITICAL"
                : emiBurdenPct >= 60 ? "HIGH" : "MEDIUM";

        double safeMaxEmi   = req.getMonthlyIncome() * (EMI_BURDEN_LIMIT / 100);
        double excessEmi    = Math.max(0, totalObligations - safeMaxEmi);
        double freeCashFlow = req.getMonthlyIncome() - req.getMonthlyExpenses() - totalObligations;

        return TrapResult.builder()
                .trapType("EMI_OVERLOAD")
                .trapName("EMI Overload Trap")
                .detected(detected)
                .severity(severity)
                .explanation("When total EMI obligations exceed 50% of monthly income, you have very little " +
                        "remaining for savings, emergencies, or discretionary spending. This creates a " +
                        "cash flow trap — any unexpected expense forces more credit card usage.")
                .consequence(detected
                        ? String.format(
                        "Your EMI burden is %.1f%% of income (₹%.0f/month in obligations on ₹%.0f income). " +
                                "You have only ₹%.0f free cash flow — insufficient for emergencies.",
                        emiBurdenPct, totalObligations, req.getMonthlyIncome(), freeCashFlow)
                        : String.format("EMI burden is healthy at %.1f%%. Good financial discipline.", emiBurdenPct))
                .recommendation(String.format(
                        "Reduce total EMI to below ₹%.0f (50%% of ₹%.0f income). " +
                                "Foreclose high-interest EMIs first. Avoid converting new purchases to EMI until obligations reduce.",
                        safeMaxEmi, req.getMonthlyIncome()))
                .currentValue(emiBurdenPct)
                .safeThreshold(EMI_BURDEN_LIMIT)
                .estimatedMonthlyCost(excessEmi)
                .potentialSaving(excessEmi * 12)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TRAP 5: Late Payment Trap
    // ─────────────────────────────────────────────────────────────────────────
    private TrapResult detectLatePaymentTrap(CreditRequest req) {
        int totalPaymentIssues = req.getLatePaymentsLastYear() + req.getMissedPaymentsLastYear();
        boolean detected       = totalPaymentIssues >= LATE_PAYMENT_WARNING;

        String severity = !detected ? "NONE"
                : req.getMissedPaymentsLastYear() >= 2 ? "CRITICAL"
                : totalPaymentIssues >= LATE_PAYMENT_HIGH_RISK ? "HIGH" : "MEDIUM";

        // Estimate annual penalty cost
        double annualLateFees  = req.getLatePaymentsLastYear() * req.getLatePamentFee();
        double scoreImpactPts  = (req.getMissedPaymentsLastYear() * 50) + (req.getLatePaymentsLastYear() * 20);

        return TrapResult.builder()
                .trapType("LATE_PAYMENT")
                .trapName("Late Payment Trap")
                .detected(detected)
                .severity(severity)
                .explanation("Payment history is the single largest factor (35%) in credit score calculation. " +
                        "Each missed payment stays on your credit report for 3+ years. Late fees " +
                        "also trigger penal interest on the entire outstanding amount.")
                .consequence(detected
                        ? String.format(
                        "You had %d late and %d missed payments in the last 12 months. " +
                                "Estimated CIBIL score impact: −%.0f points. Annual late fee cost: ₹%.0f. " +
                                "This pattern can disqualify you from new loans or force higher interest rates.",
                        req.getLatePaymentsLastYear(), req.getMissedPaymentsLastYear(),
                        scoreImpactPts, annualLateFees)
                        : "No late payment issues detected. Keep up the good behavior!")
                .recommendation("Set up auto-pay for at least the minimum due amount 3 days before the due date. " +
                        "Set calendar reminders. Consider consolidating cards to fewer accounts to simplify tracking.")
                .currentValue(totalPaymentIssues)
                .safeThreshold(0)
                .estimatedMonthlyCost(annualLateFees / 12.0)
                .potentialSaving(annualLateFees)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TRAP 6: Unsafe Practices Trap
    // ─────────────────────────────────────────────────────────────────────────
    private TrapResult detectUnsafePracticesTrap(CreditRequest req) {
        double dti = req.getMonthlyIncome() > 0
                ? ((req.getTotalOutstandingBalance() + req.getTotalEmiPerMonth() * 12 + req.getOtherLoanEmi() * 12)
                / (req.getMonthlyIncome() * 12)) * 100 : 0;

        boolean highDTI         = dti > DTI_SAFE_LIMIT;
        boolean lowCreditScore  = req.getCreditScore() < 650;
        boolean tooManyCards    = req.getNumberOfCards() > 5;
        boolean highInterestRate= req.getAnnualInterestRate() > 36;

        int riskFlags = (highDTI ? 1 : 0) + (lowCreditScore ? 1 : 0)
                + (tooManyCards ? 1 : 0) + (highInterestRate ? 1 : 0);

        boolean detected = riskFlags >= 2;
        String severity  = !detected ? "NONE"
                : riskFlags >= 3 ? "CRITICAL" : "HIGH";

        StringBuilder issueList = new StringBuilder();
        if (highDTI)          issueList.append(String.format("  • Debt-to-Income ratio: %.1f%% (safe: <40%%)\n", dti));
        if (lowCreditScore)   issueList.append(String.format("  • Credit score: %d (poor: <650)\n", req.getCreditScore()));
        if (tooManyCards)     issueList.append(String.format("  • Too many cards: %d (recommended: ≤4)\n", req.getNumberOfCards()));
        if (highInterestRate) issueList.append(String.format("  • High interest rate: %.1f%% APR\n", req.getAnnualInterestRate()));

        return TrapResult.builder()
                .trapType("UNSAFE_PRACTICES")
                .trapName("Unsafe Financial Practices Trap")
                .detected(detected)
                .severity(severity)
                .explanation("This trap identifies a combination of risky financial behaviors that together " +
                        "create systemic vulnerability — high debt load, poor credit profile, " +
                        "too many credit lines, or exploitative interest rates.")
                .consequence(detected
                        ? "Multiple unsafe financial indicators detected:\n" + issueList +
                        "This combination puts you at severe risk of a debt spiral."
                        : "No dangerous combination of financial risk factors detected.")
                .recommendation("Focus on: (1) Paying down debt to improve DTI, (2) Closing unused or " +
                        "high-fee cards, (3) Improving credit score by timely payments, " +
                        "(4) Negotiating lower interest rates with your bank after 6 months of good behavior.")
                .currentValue(dti)
                .safeThreshold(DTI_SAFE_LIMIT)
                .estimatedMonthlyCost(riskFlags * 500.0) // Proxy cost per risk flag
                .potentialSaving(riskFlags * 6000.0)
                .build();
    }
}
