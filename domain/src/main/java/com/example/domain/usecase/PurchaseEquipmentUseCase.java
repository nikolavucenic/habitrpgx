package com.example.domain.usecase;

import com.example.domain.core.Result;
import com.example.domain.repository.AuthRepository;

import java.util.concurrent.CompletableFuture;

public class PurchaseEquipmentUseCase {
    private final AuthRepository repository;

    public PurchaseEquipmentUseCase(AuthRepository repository) {
        this.repository = repository;
    }

    public CompletableFuture<Result<Void>> execute(String equipmentId, int cost) {
        return repository.purchaseEquipment(equipmentId, cost);
    }
}
