package com.example.domain.usecase;

import com.example.domain.core.Result;
import com.example.domain.model.User;
import com.example.domain.repository.AuthRepository;

public class LoginUseCase {
    private final AuthRepository repo;
    public LoginUseCase(AuthRepository repo) { this.repo = repo; }
    public Result<User> execute(String email, String pass) {
        return repo.login(email, pass);
    }
}
