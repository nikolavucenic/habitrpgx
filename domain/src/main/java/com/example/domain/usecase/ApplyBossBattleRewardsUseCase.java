package com.example.domain.usecase;

import com.example.domain.core.Result;
import com.example.domain.repository.AuthRepository;

import java.util.concurrent.CompletableFuture;

public class ApplyBossBattleRewardsUseCase {
    private final AuthRepository repository;

    public ApplyBossBattleRewardsUseCase(AuthRepository repository) {
        this.repository = repository;
    }

    public CompletableFuture<Result<Void>> execute(int earnedCoins, int earnedPp, String earnedEquipment) {
        return repository.applyBossBattleRewards(earnedCoins, earnedPp, earnedEquipment);
    }
}
