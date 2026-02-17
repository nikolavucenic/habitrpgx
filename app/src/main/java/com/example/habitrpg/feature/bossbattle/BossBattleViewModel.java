package com.example.habitrpg.feature.bossbattle;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.example.domain.core.Result;
import com.example.domain.model.User;
import com.example.domain.progression.BossBattleCalculator;
import com.example.domain.usecase.GetCurrentUserProfileUseCase;
import com.example.domain.usecase.GetStageSuccessRateUseCase;
import com.example.habitrpg.core.CoreViewModel;

import java.util.Random;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class BossBattleViewModel extends CoreViewModel<BossBattleUiState, BossBattleAction, BossBattleSideEffect> {

    private static final String KEY_BOSS_NUMBER = "boss_number";
    private static final String KEY_BOSS_HP = "boss_hp";

    private final GetCurrentUserProfileUseCase getCurrentUserProfileUseCase;
    private final GetStageSuccessRateUseCase getStageSuccessRateUseCase;
    private final SharedPreferences sharedPreferences;
    private final Random random = new Random();

    @Inject
    public BossBattleViewModel(GetCurrentUserProfileUseCase getCurrentUserProfileUseCase,
                               GetStageSuccessRateUseCase getStageSuccessRateUseCase,
                               SharedPreferences sharedPreferences) {
        this.getCurrentUserProfileUseCase = getCurrentUserProfileUseCase;
        this.getStageSuccessRateUseCase = getStageSuccessRateUseCase;
        this.sharedPreferences = sharedPreferences;
        state.setValue(BossBattleUiState.loading());
    }

    @Override
    public void handleAction(BossBattleAction action) {
        if (action instanceof BossBattleAction.OnScreenStarted) {
            loadBattle();
        } else if (action instanceof BossBattleAction.OnAttackClicked || action instanceof BossBattleAction.OnShakeAttackTriggered) {
            processAttack();
        } else if (action instanceof BossBattleAction.OnShakeChestTriggered) {
            openChest();
        } else if (action instanceof BossBattleAction.OnActivateEquipmentClicked) {
            sideEffect.setValue(new BossBattleSideEffect.ShowToast("Oprema stiže sa Feature #6."));
        }
    }

    private void loadBattle() {
        state.setValue(BossBattleUiState.loading());
        getCurrentUserProfileUseCase.execute().thenAccept(profileResult ->
                getStageSuccessRateUseCase.execute().thenAccept(successResult ->
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (profileResult instanceof Result.Error) {
                                sideEffect.setValue(new BossBattleSideEffect.ShowToast(((Result.Error<User>) profileResult).message));
                                return;
                            }

                            User user = ((Result.Success<User>) profileResult).data;
                            int chance = 0;
                            if (successResult instanceof Result.Success) {
                                chance = ((Result.Success<Integer>) successResult).data;
                            }

                            int targetBossNumber = Math.max(1, user.level - 1);
                            int savedBossNumber = sharedPreferences.getInt(KEY_BOSS_NUMBER, 0);
                            int bossNumber = savedBossNumber > 0 && savedBossNumber <= targetBossNumber
                                    ? savedBossNumber
                                    : targetBossNumber;

                            int maxHp = BossBattleCalculator.hpForBoss(bossNumber);
                            int savedHp = sharedPreferences.getInt(KEY_BOSS_HP, maxHp);
                            int currentHp = bossNumber == savedBossNumber ? Math.min(savedHp, maxHp) : maxHp;
                            if (currentHp <= 0) currentHp = maxHp;

                            persistBossState(bossNumber, currentHp);

                            state.setValue(new BossBattleUiState(
                                    false,
                                    bossNumber,
                                    maxHp,
                                    currentHp,
                                    Math.max(0, user.pp),
                                    Math.max(0, Math.min(100, chance)),
                                    5,
                                    false,
                                    false,
                                    0,
                                    null,
                                    false,
                                    "Borba je počela. Napadni bosa!"
                            ));
                        })
                )
        );
    }

    private void processAttack() {
        BossBattleUiState current = state.getValue();
        if (current == null || current.loading || current.battleFinished) return;
        if (current.attacksLeft <= 0) return;

        int attacksLeft = current.attacksLeft - 1;
        int roll = random.nextInt(100);
        boolean hit = roll < current.successChance;

        int nextHp = current.bossCurrentHp;
        String message;
        if (hit) {
            nextHp = Math.max(0, nextHp - current.userPp);
            message = "Pogodak! Boss prima " + current.userPp + " štete.";
            sideEffect.setValue(new BossBattleSideEffect.PlayBossHitAnimation());
        } else {
            message = "Promašaj! Boss je izbegao napad.";
            sideEffect.setValue(new BossBattleSideEffect.PlayBossMissAnimation());
        }

        boolean finished = nextHp <= 0 || attacksLeft == 0;
        boolean won = finished && nextHp <= 0;

        int earnedCoins = 0;
        String earnedEquipment = null;
        boolean chestOpened = false;

        if (finished) {
            Reward reward = calculateReward(current.bossNumber, current.bossMaxHp, nextHp, won);
            earnedCoins = reward.coins;
            earnedEquipment = reward.equipment;
            message = won ? "Boss je poražen! Protresi telefon da otvoriš kovčeg." : "Borba završena. Protresi telefon za rezultat.";
            persistAfterBattle(current.bossNumber, won, nextHp);
        } else {
            persistBossState(current.bossNumber, nextHp);
        }

        state.setValue(new BossBattleUiState(
                false,
                current.bossNumber,
                current.bossMaxHp,
                nextHp,
                current.userPp,
                current.successChance,
                attacksLeft,
                finished,
                won,
                earnedCoins,
                earnedEquipment,
                chestOpened,
                message
        ));
    }

    private void openChest() {
        BossBattleUiState current = state.getValue();
        if (current == null || !current.battleFinished || current.chestOpened) return;
        sideEffect.setValue(new BossBattleSideEffect.PlayChestShakeAnimation());
        state.setValue(new BossBattleUiState(
                current.loading,
                current.bossNumber,
                current.bossMaxHp,
                current.bossCurrentHp,
                current.userPp,
                current.successChance,
                current.attacksLeft,
                current.battleFinished,
                current.battleWon,
                current.earnedCoins,
                current.earnedEquipment,
                true,
                current.battleWon ? "Kovčeg je otvoren!" : "Utešna nagrada je spremna."
        ));
    }

    private Reward calculateReward(int bossNumber, int bossMaxHp, int remainingHp, boolean won) {
        int coins = BossBattleCalculator.coinsForBoss(bossNumber);
        int chancePercent = 20;

        if (!won) {
            int damaged = bossMaxHp - remainingHp;
            if (damaged >= bossMaxHp / 2) {
                coins = Math.round(coins / 2f);
                chancePercent = 10;
            } else {
                coins = 0;
                chancePercent = 0;
            }
        }

        String equipment = null;
        if (random.nextInt(100) < chancePercent) {
            equipment = random.nextInt(100) < 95 ? "Odeća" : "Oružje";
        }

        return new Reward(coins, equipment);
    }

    private void persistAfterBattle(int currentBossNumber, boolean won, int remainingHp) {
        if (won) {
            int nextBossNumber = currentBossNumber + 1;
            persistBossState(nextBossNumber, BossBattleCalculator.hpForBoss(nextBossNumber));
        } else {
            persistBossState(currentBossNumber, remainingHp);
        }
    }

    private void persistBossState(int bossNumber, int hp) {
        sharedPreferences.edit()
                .putInt(KEY_BOSS_NUMBER, bossNumber)
                .putInt(KEY_BOSS_HP, hp)
                .apply();
    }

    private static class Reward {
        final int coins;
        final String equipment;

        Reward(int coins, String equipment) {
            this.coins = coins;
            this.equipment = equipment;
        }
    }
}
