package com.example.domain.usecase;

import com.example.domain.repository.TaskRepository;

public class IsPendingBossEncounterUseCase {
    private final TaskRepository repository;

    public IsPendingBossEncounterUseCase(TaskRepository repository) {
        this.repository = repository;
    }

    public boolean execute() {
        return repository.isPendingBossEncounter();
    }
}
