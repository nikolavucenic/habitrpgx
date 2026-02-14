package com.example.domain.usecase;

import com.example.domain.core.Result;
import com.example.domain.repository.AuthRepository;

import java.util.concurrent.CompletableFuture;

public class LogoutUseCase {
    private final AuthRepository repository;

    public LogoutUseCase(AuthRepository repository) {
        this.repository = repository;
    }

    public CompletableFuture<Result<Void>> execute() {
        return repository.logout();
    }
}
