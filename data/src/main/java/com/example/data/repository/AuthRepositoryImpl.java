package com.example.data.repository;

import com.example.data.remote.firebase.FirebaseService;
import com.example.domain.core.Result;
import com.example.domain.model.ActivationToken;
import com.example.domain.model.RegistrationRequest;
import com.example.domain.model.User;
import com.example.domain.repository.AuthRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuthRepositoryImpl implements AuthRepository {
    private final FirebaseService firebaseService;

    @Inject
    public AuthRepositoryImpl(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

    @Override
    public Result<String> register(RegistrationRequest request) {
        return new Result.Error<>("Not implemented in S1-0 skeleton");
    }

    @Override
    public Result<ActivationToken> createActivationToken(String userId) {
        return new Result.Error<>("Not implemented in S1-0 skeleton");
    }

    @Override
    public Result<Void> activate(String token) {
        return new Result.Error<>("Not implemented in S1-0 skeleton");
    }

    @Override
    public Result<Boolean> isUserActive(String userId) {
        return new Result.Error<>("Not implemented in S1-0 skeleton");
    }

    @Override
    public Result<User> login(String email, String password) {
        return new Result.Error<>("Not implemented in S1-0 skeleton");
    }

    @Override
    public Result<Void> logout() {
        firebaseService.auth().signOut();
        return new Result.Success<>(null);
    }

    @Override
    public Result<User> getCurrentUserProfile() {
        return new Result.Error<>("Not implemented in S1-0 skeleton");
    }

    @Override
    public Result<Void> changePassword(String oldPassword, String newPassword) {
        return new Result.Error<>("Not implemented in S1-0 skeleton");
    }

    @Override
    public Result<Boolean> isEmailVerified() {
        return new Result.Error<>("Not implemented in S1-0 skeleton");
    }

    @Override
    public Result<Void> resendVerificationEmail() {
        return new Result.Error<>("Not implemented in S1-0 skeleton");
    }
}
