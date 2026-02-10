package com.example.domain.repository;

import com.example.domain.core.Result;
import com.example.domain.model.User;

public interface AuthRepository {
    Result<Void> register(String email, String password, String username, int avatarId);
    Result<User> login(String email, String password);
    Result<Void> logout();
    Result<User> getCurrentUserProfile();
    Result<Void> changePassword(String oldPassword, String newPassword);
    Result<Boolean> isEmailVerified();
    Result<Void> resendVerificationEmail();
}
