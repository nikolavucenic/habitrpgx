package com.example.domain.usecase;

import com.example.domain.repository.AuthRepository;

public class IsLoggedInUseCase {
    private final AuthRepository repository;

    public IsLoggedInUseCase(AuthRepository repository) {
        this.repository = repository;
    }

    public boolean execute() {
        return repository.isLoggedIn();
    }
}
