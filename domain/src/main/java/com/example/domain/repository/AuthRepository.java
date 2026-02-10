package com.example.domain.repository;

import com.example.domain.core.Result;
import com.example.domain.model.ActivationToken;
import com.example.domain.model.RegistrationRequest;
import com.example.domain.model.User;

public interface AuthRepository {
    // EPIC 1.1
    Result<String> register(RegistrationRequest request);
    Result<ActivationToken> createActivationToken(String userId);
    Result<Void> activate(String token);
    Result<Boolean> isUserActive(String userId);

    // Existing contracts kept for backward compatibility with pending epics.
    Result<User> login(String email, String password);
    Result<Void> logout();
    Result<User> getCurrentUserProfile();
    Result<Void> changePassword(String oldPassword, String newPassword);
    Result<Boolean> isEmailVerified();
    Result<Void> resendVerificationEmail();
}
