package com.example.habitrpg.feature.bossbattle;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.example.domain.core.Result;
import com.example.domain.model.User;
import com.example.domain.progression.BossBattleCalculator;
import com.example.domain.usecase.ApplyBossBattleRewardsUseCase;
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
    private static final String KEY_LAST_RESOLVED_BOSS_ENCOUNTER_LEVEL = "last_resolved_boss_encounter_level";

    private final GetCurrentUserProfileUseCase getCurrentUserProfileUseCase;
    private final GetStageSuccessRateUseCase getStageSuccessRateUseCase;
    private final ApplyBossBattleRewardsUseCase applyBossBattleRewardsUseCase;
    private final SharedPreferences sharedPreferences;
    private final Random random = new Random();

    @Inject
    public BossBattleViewModel(GetCurrentUserProfileUseCase getCurrentUserProfileUseCase,
                               GetStageSuccessRateUseCase getStageSuccessRateUseCase,
                               ApplyBossBattleRewardsUseCase applyBossBattleRewardsUseCase,
                               SharedPreferences sharedPreferences) {
        this.getCurrentUserProfileUseCase = getCurrentUserProfileUseCase;
        this.getStageSuccessRateUseCase = getStageSuccessRateUseCase;
        this.applyBossBattleRewardsUseCase = applyBossBattleRewardsUseCase;
        this.sharedPreferences = sharedPreferences;
        state.setValue(new BossBattleUiState.Loading(BossBattleUiState.initialData()));
    }

    @Override
    public void handleAction(BossBattleAction action) {
        if (action instanceof BossBattleAction.OnScreenStarted) {
            loadBattle();
        } else if (action instanceof BossBattleAction.OnAttackClicked || action instanceof BossBattleAction.OnShakeAttackTriggered) {
            processAttack();
        } else if (action instanceof BossBattleAction.OnShakeChestTriggered) {
            openChest();
        } else if (action instanceof BossBattleAction.OnContinueClicked) {
            continueAfterBattle();
        } else if (action instanceof BossBattleAction.OnActivateEquipmentClicked) {
            sideEffect.setValue(new BossBattleSideEffect.ShowToast("Oprema stiže sa Feature #6."));
        }
    }

    private void loadBattle() {
        BossBattleUiState.Data currentData = getDataOrInitial();
        state.setValue(new BossBattleUiState.Loading(currentData));
        getCurrentUserProfileUseCase.execute().thenAccept(profileResult ->
                getStageSuccessRateUseCase.execute().thenAccept(successResult ->
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (profileResult instanceof Result.Error) {
                                state.setValue(new BossBattleUiState.Error(currentData, ((Result.Error<User>) profileResult).message));
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

                            state.setValue(new BossBattleUiState.Active(new BossBattleUiState.Data(
                                    bossNumber,
                                    user.level,
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
                                    false,
                                    "Borba je počela. Napadni bosa!"
                            )));
                        })
                )
        );
    }

    private void processAttack() {
        BossBattleUiState current = state.getValue();
        BossBattleUiState.Data currentData = current != null ? current.getData() : null;
        if (currentData == null || current instanceof BossBattleUiState.Loading || currentData.battleFinished) return;
        if (currentData.attacksLeft <= 0) return;

        int attacksLeft = currentData.attacksLeft - 1;
        int roll = random.nextInt(100);
        boolean hit = roll < currentData.successChance;

        int nextHp = currentData.bossCurrentHp;
        String message;
        if (hit) {
            nextHp = Math.max(0, nextHp - currentData.userPp);
            message = "Pogodak! Boss prima " + currentData.userPp + " štete.";
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
        boolean rewardsApplied = false;

        if (finished) {
            Reward reward = calculateReward(currentData.bossNumber, currentData.bossMaxHp, nextHp, won);
            earnedCoins = reward.coins;
            earnedEquipment = reward.equipment;
            message = won ? "Boss je poražen! Protresi telefon da otvoriš kovčeg." : "Borba završena. Protresi telefon za rezultat.";
            persistAfterBattle(currentData.bossNumber, won, nextHp);
            persistResolvedEncounterLevel(currentData.encounterLevel);
        } else {
            persistBossState(currentData.bossNumber, nextHp);
        }

        state.setValue(new BossBattleUiState.Active(new BossBattleUiState.Data(
                currentData.bossNumber,
                currentData.encounterLevel,
                currentData.bossMaxHp,
                nextHp,
                currentData.userPp,
                currentData.successChance,
                attacksLeft,
                finished,
                won,
                earnedCoins,
                earnedEquipment,
                chestOpened,
                rewardsApplied,
                message
        )));
    }

    private void openChest() {
        BossBattleUiState.Data current = getCurrentData();
        if (current == null || !current.battleFinished || current.chestOpened) return;
        sideEffect.setValue(new BossBattleSideEffect.PlayChestShakeAnimation());

        BossBattleUiState.Data opened = copyData(current, true, current.rewardsApplied,
                current.battleWon ? "Kovčeg je otvoren!" : "Utešna nagrada je spremna.");
        state.setValue(new BossBattleUiState.Active(opened));

        if (!current.rewardsApplied) {
            applyRewards(opened);
        }
    }

    private void applyRewards(BossBattleUiState.Data currentData) {
        applyBossBattleRewardsUseCase.execute(currentData.earnedCoins, currentData.earnedEquipment)
                .thenAccept(result -> new Handler(Looper.getMainLooper()).post(() -> {
                    BossBattleUiState.Data latest = getCurrentData();
                    if (latest == null) return;

                    if (result instanceof Result.Error) {
                        sideEffect.setValue(new BossBattleSideEffect.ShowToast(((Result.Error<Void>) result).message));
                        return;
                    }

                    state.setValue(new BossBattleUiState.Active(copyData(latest, latest.chestOpened, true, latest.battleMessage)));
                }));
    }

    private void continueAfterBattle() {
        BossBattleUiState.Data current = getCurrentData();
        if (current == null || !current.chestOpened) return;
        if (!current.rewardsApplied) {
            sideEffect.setValue(new BossBattleSideEffect.ShowToast("Sačekaj trenutak da se nagrade sačuvaju."));
            return;
        }
        sideEffect.setValue(new BossBattleSideEffect.NavigateBack());
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

    private void persistResolvedEncounterLevel(int encounterLevel) {
        int safeLevel = Math.max(1, encounterLevel);
        int currentLockedLevel = sharedPreferences.getInt(KEY_LAST_RESOLVED_BOSS_ENCOUNTER_LEVEL, 0);
        if (safeLevel <= currentLockedLevel) return;

        sharedPreferences.edit()
                .putInt(KEY_LAST_RESOLVED_BOSS_ENCOUNTER_LEVEL, safeLevel)
                .apply();
    }

    private BossBattleUiState.Data getCurrentData() {
        BossBattleUiState current = state.getValue();
        return current != null ? current.getData() : null;
    }

    private BossBattleUiState.Data getDataOrInitial() {
        BossBattleUiState.Data data = getCurrentData();
        return data != null ? data : BossBattleUiState.initialData();
    }

    private BossBattleUiState.Data copyData(BossBattleUiState.Data current,
                                            boolean chestOpened,
                                            boolean rewardsApplied,
                                            String message) {
        return new BossBattleUiState.Data(
                current.bossNumber,
                current.encounterLevel,
                current.bossMaxHp,
                current.bossCurrentHp,
                current.userPp,
                current.successChance,
                current.attacksLeft,
                current.battleFinished,
                current.battleWon,
                current.earnedCoins,
                current.earnedEquipment,
                chestOpened,
                rewardsApplied,
                message
        );
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
