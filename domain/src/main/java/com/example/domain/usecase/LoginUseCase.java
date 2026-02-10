package com.example.domain.usecase;

import com.example.domain.core.Result;
import com.example.domain.model.User;
import com.example.domain.repository.AuthRepository;

public class LoginUseCase {
    private final AuthRepository repo;
    public LoginUseCase(AuthRepository repo) { this.repo = repo; }

    public Result<User> execute(String email, String pass) {
        Result<User> loginResult = repo.login(email, pass);
        if (loginResult instanceof Result.Error) {
            return loginResult;
        }

        User user = ((Result.Success<User>) loginResult).data;
        Result<Boolean> activeResult = repo.isUserActive(user.uid);
        if (activeResult instanceof Result.Error) {
            return new Result.Error<>(((Result.Error<Boolean>) activeResult).message);
        }

        boolean isActive = ((Result.Success<Boolean>) activeResult).data;
        if (!isActive) {
            return new Result.Error<>("Account is not activated. Please activate your account before login.");
        }

        return loginResult;
    }
}
