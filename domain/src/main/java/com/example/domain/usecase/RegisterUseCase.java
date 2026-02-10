package com.example.domain.usecase;

import com.example.domain.core.Result;
import com.example.domain.model.RegistrationRequest;
import com.example.domain.repository.AuthRepository;

public class RegisterUseCase {
    private final AuthRepository repo;

    public RegisterUseCase(AuthRepository repo) {
        this.repo = repo;
    }

    public Result<String> execute(String email, String pass, String confirmPass, String username, int avatarId) {
        return repo.register(new RegistrationRequest(email, pass, confirmPass, username, avatarId));
    }
}
