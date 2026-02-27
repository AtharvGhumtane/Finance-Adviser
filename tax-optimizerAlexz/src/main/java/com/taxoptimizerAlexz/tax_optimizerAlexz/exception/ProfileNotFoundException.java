package com.taxoptimizerAlexz.tax_optimizerAlexz.exception;

public class ProfileNotFoundException extends RuntimeException {
    public ProfileNotFoundException(Long id) {
        super("Tax profile not found with id: " + id);
    }
    public ProfileNotFoundException(String userId) {
        super("No profiles found for userId: " + userId);
    }
}
