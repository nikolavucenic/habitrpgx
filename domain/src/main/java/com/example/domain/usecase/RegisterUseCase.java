package com.example.domain.usecase;

import com.example.domain.core.Result;
import com.example.domain.repository.AuthRepository;

public class RegisterUseCase {
    private final AuthRepository repo;
    public RegisterUseCase(AuthRepository repo) { this.repo = repo; }
    public Result<Void> execute(String email, String pass, String username, int avatarId) {
        return repo.register(email, pass, username, avatarId);
    }
}
