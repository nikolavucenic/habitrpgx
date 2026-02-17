package com.example.domain.usecase;

import com.example.domain.repository.TaskRepository;

public class GetLastResolvedBossEncounterLevelUseCase {
    private final TaskRepository repository;

    public GetLastResolvedBossEncounterLevelUseCase(TaskRepository repository) {
        this.repository = repository;
    }

    public int execute() {
        return repository.getLastResolvedBossEncounterLevel();
    }
}
