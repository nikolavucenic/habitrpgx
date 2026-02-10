package com.example.domain.error;

public enum RegistrationError {
    INVALID_EMAIL("Email format is invalid."),
    PASSWORD_MISMATCH("Password and confirmation must match."),
    INVALID_USERNAME("Username is invalid."),
    INVALID_AVATAR("Avatar must be in range 1..5."),
    REPOSITORY_FAILURE("Registration failed due to repository error.");

    private final String message;

    RegistrationError(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
