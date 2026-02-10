package com.example.domain.validator;

public class UsernameValidator {

    public boolean isValid(String username) {
        if (username == null) return false;
        String trimmed = username.trim();
        return !trimmed.isEmpty() && trimmed.length() >= 3 && trimmed.length() <= 20;
    }
}
