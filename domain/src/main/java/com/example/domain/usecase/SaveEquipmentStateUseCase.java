package com.example.domain.usecase;

import com.example.domain.core.Result;
import com.example.domain.repository.AuthRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SaveEquipmentStateUseCase {
    private final AuthRepository repository;

    public SaveEquipmentStateUseCase(AuthRepository repository) {
        this.repository = repository;
    }

    public CompletableFuture<Result<Void>> execute(List<String> equipment, int coins) {
        return repository.saveEquipmentState(equipment, coins);
    }
}
