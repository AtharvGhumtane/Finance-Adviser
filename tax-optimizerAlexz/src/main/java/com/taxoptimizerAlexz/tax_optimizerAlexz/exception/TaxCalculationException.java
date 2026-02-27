package com.taxoptimizerAlexz.tax_optimizerAlexz.exception;

public class TaxCalculationException extends RuntimeException {
    public TaxCalculationException(String message) {
        super(message);
    }
    public TaxCalculationException(String message, Throwable cause) {
        super(message, cause);
    }
}
