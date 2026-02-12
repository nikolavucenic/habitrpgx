package com.example.domain.usecase;

import com.example.domain.core.Result;
import com.example.domain.model.User;
import com.example.domain.repository.AuthRepository;
import java.util.concurrent.CompletableFuture;

public class LoginUseCase {
    private final AuthRepository repository;

    public LoginUseCase(AuthRepository repository) {
        this.repository = repository;
    }

    public CompletableFuture<Result<User>> execute(String email, String password) {
        return repository.login(email, password);
    }
}