package com.example.domain.usecase;

import com.example.domain.core.Result;
import com.example.domain.model.User;
import com.example.domain.repository.AuthRepository;

import java.util.concurrent.CompletableFuture;

public class GetCurrentUserProfileUseCase {
    private final AuthRepository repository;

    public GetCurrentUserProfileUseCase(AuthRepository repository) {
        this.repository = repository;
    }

    public CompletableFuture<Result<User>> execute() {
        return repository.getCurrentUserProfile();
    }
}
