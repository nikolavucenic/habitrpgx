package com.example.domain.usecase;

import com.example.domain.repository.TaskRepository;

public class GetBossHpUseCase {
    private final TaskRepository repository;

    public GetBossHpUseCase(TaskRepository repository) {
        this.repository = repository;
    }

    public int execute() {
        return repository.getBossHp();
    }
}
