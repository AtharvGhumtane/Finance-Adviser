package com.creditcard.credit_card_service.exeception;

public class CreditAnalysisException extends RuntimeException {
    public CreditAnalysisException(String message) { super(message); }
    public CreditAnalysisException(String message, Throwable cause) { super(message, cause); }
}
