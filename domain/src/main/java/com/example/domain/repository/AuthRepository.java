package com.example.domain.repository;

import com.example.domain.core.Result;
import com.example.domain.model.User;

import java.util.concurrent.CompletableFuture;

public interface AuthRepository {
    boolean isLoggedIn();
    CompletableFuture<Result<Void>> register(String email, String password, String username, int avatarId);
    CompletableFuture<Result<User>> login(String email, String password);
    CompletableFuture<Result<Void>> logout();
    CompletableFuture<Result<User>> getCurrentUserProfile();
    CompletableFuture<Result<Void>> changePassword(String oldPassword, String newPassword);
    CompletableFuture<Result<Boolean>> isEmailVerified();
    CompletableFuture<Result<Void>> resendVerificationEmail();
    CompletableFuture<Result<Void>> applyBossBattleRewards(int earnedCoins, String earnedEquipment);
}
