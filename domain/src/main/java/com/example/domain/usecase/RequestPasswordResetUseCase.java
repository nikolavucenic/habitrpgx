package com.example.domain.usecase;

import com.example.domain.core.Result;
import com.example.domain.repository.AuthRepository;

import java.util.concurrent.CompletableFuture;

public class RequestPasswordResetUseCase {
    private final AuthRepository repository;

    public RequestPasswordResetUseCase(AuthRepository repository) {
        this.repository = repository;
    }

    public CompletableFuture<Result<Void>> execute(String email) {
        return repository.requestPasswordReset(email);
    }
}
