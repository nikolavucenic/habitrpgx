package com.example.domain.usecase;

import com.example.domain.core.Result;
import com.example.domain.model.User;
import com.example.domain.repository.AuthRepository;

public class GetProfileUseCase {
    private final AuthRepository repo;
    public GetProfileUseCase(AuthRepository repo) { this.repo = repo; }
    public Result<User> execute() {
        return repo.getCurrentUserProfile();
    }
}