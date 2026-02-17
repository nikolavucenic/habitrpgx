package com.example.domain.usecase;

import com.example.domain.repository.TaskRepository;

public class SaveLastResolvedBossEncounterLevelUseCase {
    private final TaskRepository repository;

    public SaveLastResolvedBossEncounterLevelUseCase(TaskRepository repository) {
        this.repository = repository;
    }

    public void execute(int encounterLevel) {
        repository.saveLastResolvedBossEncounterLevel(encounterLevel);
    }
}
