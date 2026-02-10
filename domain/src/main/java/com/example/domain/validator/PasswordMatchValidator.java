package com.example.domain.validator;

public class PasswordMatchValidator {

    public boolean isValid(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword);
    }
}
