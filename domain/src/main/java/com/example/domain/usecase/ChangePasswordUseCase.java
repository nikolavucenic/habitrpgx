package com.example.domain.usecase;

import com.example.domain.core.Result;
import com.example.domain.repository.AuthRepository;

public class ChangePasswordUseCase {
    private final AuthRepository repo;
    public ChangePasswordUseCase(AuthRepository repo) { this.repo = repo; }
    public Result<Void> execute(String oldPass, String newPass) {
        return repo.changePassword(oldPass, newPass);
    }
}
