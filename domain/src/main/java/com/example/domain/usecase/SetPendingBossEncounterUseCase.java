package com.example.domain.usecase;

import com.example.domain.repository.TaskRepository;

public class SetPendingBossEncounterUseCase {
    private final TaskRepository repository;

    public SetPendingBossEncounterUseCase(TaskRepository repository) {
        this.repository = repository;
    }

    public void execute(boolean pending) {
        repository.setPendingBossEncounter(pending);
    }
}
