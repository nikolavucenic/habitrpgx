package com.example.domain.usecase;

import com.example.domain.core.Result;
import com.example.domain.repository.AuthRepository;

import java.util.concurrent.CompletableFuture;

public class ChangePasswordUseCase {
    private final AuthRepository repository;

    public ChangePasswordUseCase(AuthRepository repository) {
        this.repository = repository;
    }

    public CompletableFuture<Result<Void>> execute(String oldPassword, String newPassword) {
        return repository.changePassword(oldPassword, newPassword);
    }
}
