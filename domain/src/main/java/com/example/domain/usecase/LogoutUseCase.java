package com.example.domain.usecase;

import com.example.domain.core.Result;
import com.example.domain.repository.AuthRepository;

public class LogoutUseCase {
    private final AuthRepository repo;
    public LogoutUseCase(AuthRepository repo) { this.repo = repo; }
    public Result<Void> execute() {
        return repo.logout();
    }
}

