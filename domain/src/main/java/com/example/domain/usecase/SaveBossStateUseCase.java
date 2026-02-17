package com.example.domain.usecase;

import com.example.domain.repository.TaskRepository;

public class SaveBossStateUseCase {
    private final TaskRepository repository;

    public SaveBossStateUseCase(TaskRepository repository) {
        this.repository = repository;
    }

    public void execute(int bossNumber, int hp) {
        repository.saveBossState(bossNumber, hp);
    }
}
