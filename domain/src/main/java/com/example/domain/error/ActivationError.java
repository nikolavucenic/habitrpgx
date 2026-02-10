package com.example.domain.error;

public enum ActivationError {
    TOKEN_EXPIRED("Activation token expired (valid for 24 hours)."),
    INVALID_TOKEN("Activation token is invalid."),
    REPOSITORY_FAILURE("Activation failed due to repository error.");

    private final String message;

    ActivationError(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
