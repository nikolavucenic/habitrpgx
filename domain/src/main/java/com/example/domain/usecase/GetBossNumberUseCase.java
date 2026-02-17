package com.example.domain.usecase;

import com.example.domain.repository.TaskRepository;

public class GetBossNumberUseCase {
    private final TaskRepository repository;

    public GetBossNumberUseCase(TaskRepository repository) {
        this.repository = repository;
    }

    public int execute() {
        return repository.getBossNumber();
    }
}
