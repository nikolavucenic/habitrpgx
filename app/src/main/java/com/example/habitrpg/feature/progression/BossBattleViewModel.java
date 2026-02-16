package com.example.habitrpg.feature.progression;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.SavedStateHandle;

import com.example.domain.core.Result;
import com.example.domain.model.TaskItem;
import com.example.domain.model.User;
import com.example.domain.progression.BossBattleCalculator;
import com.example.domain.usecase.GetCurrentUserProfileUseCase;
import com.example.domain.usecase.GetTasksUseCase;
import com.example.habitrpg.core.CoreViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class BossBattleViewModel extends CoreViewModel<BossBattleUiState, BossBattleAction, Void> {

    private static final String KEY_DEFEATED_BOSS_PREFIX = "defeated_boss_";
    private static final String KEY_ACTIVE_BOSS_INDEX_PREFIX = "active_boss_index_";
    private static final String KEY_ACTIVE_BOSS_HP_PREFIX = "active_boss_hp_";

    private final GetCurrentUserProfileUseCase getCurrentUserProfileUseCase;
    private final GetTasksUseCase getTasksUseCase;
    private final SharedPreferences sharedPreferences;
    private final Random random = new Random();

    private String uid = "";

    @Inject
    public BossBattleViewModel(GetCurrentUserProfileUseCase getCurrentUserProfileUseCase,
                               GetTasksUseCase getTasksUseCase,
                               SharedPreferences sharedPreferences,
                               SavedStateHandle savedStateHandle) {
        this.getCurrentUserProfileUseCase = getCurrentUserProfileUseCase;
        this.getTasksUseCase = getTasksUseCase;
        this.sharedPreferences = sharedPreferences;
        state.setValue(new BossBattleUiState.Loading(BossBattleUiState.initialData()));
    }

    @Override
    public void handleAction(BossBattleAction action) {
        if (action instanceof BossBattleAction.Load) {
            load();
        } else if (action instanceof BossBattleAction.OnEquipmentToggle) {
            toggleEquipment(((BossBattleAction.OnEquipmentToggle) action).enabled);
        } else if (action instanceof BossBattleAction.OnAttackClicked || action instanceof BossBattleAction.OnShakeAttack) {
            attack();
        } else if (action instanceof BossBattleAction.OnOpenChest) {
            openChest();
        }
    }

    private void load() {
        BossBattleUiState current = state.getValue();
        BossBattleUiState.Data currentData = current != null ? current.getData() : BossBattleUiState.initialData();
        state.setValue(new BossBattleUiState.Loading(currentData));

        getCurrentUserProfileUseCase.execute().thenAccept(profileResult ->
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (profileResult instanceof Result.Error) {
                        state.setValue(new BossBattleUiState.Error(currentData, ((Result.Error<User>) profileResult).message));
                        return;
                    }

                    User user = ((Result.Success<User>) profileResult).data;
                    uid = user.uid;

                    int defeatedBosses = readDefeatedBossCount();
                    int pendingBossIndex = defeatedBosses + 1;
                    boolean available = pendingBossIndex <= Math.max(1, user.level);
                    if (!available) {
                        BossBattleUiState.Data data = new BossBattleUiState.Data(
                                pendingBossIndex,
                                0,
                                BossBattleCalculator.bossHpForIndex(pendingBossIndex),
                                0,
                                0,
                                Math.max(user.pp, 0),
                                user.equipment != null && !user.equipment.isEmpty() ? user.equipment.get(0) : "Bez opreme",
                                false,
                                "Nema aktivnog boss encounter-a.",
                                true,
                                0,
                                null,
                                false,
                                false
                        );
                        state.setValue(new BossBattleUiState.Success(data));
                        return;
                    }

                    getTasksUseCase.execute().thenAccept(tasksResult ->
                            new Handler(Looper.getMainLooper()).post(() -> {
                                List<TaskItem> tasks = new ArrayList<>();
                                if (tasksResult instanceof Result.Success) {
                                    tasks = ((Result.Success<List<TaskItem>>) tasksResult).data;
                                }

                                int successRate = BossBattleCalculator.calculateSuccessRate(tasks);
                                int bossIndex = pendingBossIndex;
                                int bossMaxHp = BossBattleCalculator.bossHpForIndex(bossIndex);
                                int savedIndex = readActiveBossIndex(bossIndex);
                                int savedHp = readActiveBossHp(bossMaxHp);
                                int hp = (savedIndex == bossIndex) ? Math.max(0, Math.min(bossMaxHp, savedHp)) : bossMaxHp;

                                BossBattleUiState.Data data = new BossBattleUiState.Data(
                                        bossIndex,
                                        hp,
                                        bossMaxHp,
                                        BossBattleCalculator.ATTEMPTS_PER_BATTLE,
                                        successRate,
                                        Math.max(user.pp, 0),
                                        user.equipment != null && !user.equipment.isEmpty() ? user.equipment.get(0) : "Bez opreme",
                                        false,
                                        "Boss encounter je aktivan."
                                                + (hp < bossMaxHp ? " Nastavljaš prethodnu borbu." : ""),
                                        false,
                                        0,
                                        null,
                                        false,
                                        true
                                );
                                state.setValue(new BossBattleUiState.Success(data));
                            })
                    );
                })
        );
    }

    private void toggleEquipment(boolean enabled) {
        BossBattleUiState current = state.getValue();
        if (current == null) return;
        BossBattleUiState.Data data = current.getData();
        if (!data.battleAvailable || data.battleFinished) return;

        boolean canActivate = !"Bez opreme".equals(data.equippedItem);
        boolean activated = enabled && canActivate;

        state.setValue(new BossBattleUiState.Success(data.copy(
                data.bossHp,
                data.attacksLeft,
                activated,
                activated ? "Oprema aktivirana." : "Oprema nije aktivirana.",
                false,
                0,
                null,
                false
        )));
    }

    private void attack() {
        BossBattleUiState current = state.getValue();
        if (current == null) return;
        BossBattleUiState.Data data = current.getData();

        if (!data.battleAvailable || data.battleFinished || data.attacksLeft <= 0) return;

        boolean hit = BossBattleCalculator.isAttackSuccessful(data.successRate, random);
        int newHp = data.bossHp;
        String message;

        if (hit) {
            newHp = Math.max(0, data.bossHp - Math.max(1, data.effectivePp));
            message = "Direktan pogodak!";
        } else {
            message = "Promašaj! Bos izbegava napad.";
        }

        int attacksLeft = data.attacksLeft - 1;
        boolean finished = attacksLeft == 0 || newHp == 0;

        if (!finished) {
            saveActiveBoss(data.bossIndex, newHp);
            state.setValue(new BossBattleUiState.Success(data.copy(
                    newHp, attacksLeft, data.equipmentActivated, message, false, 0, null, false
            )));
            return;
        }

        resolveBattle(data, newHp);
    }

    private void resolveBattle(BossBattleUiState.Data data, int finalHp) {
        boolean defeated = finalHp == 0;
        int baseCoins = BossBattleCalculator.coinsForBossIndex(data.bossIndex);
        int wonCoins = 0;
        boolean halfReward = false;

        if (defeated) {
            wonCoins = baseCoins;
        } else {
            int damage = data.bossMaxHp - finalHp;
            if (damage >= data.bossMaxHp / 2) {
                halfReward = true;
                wonCoins = BossBattleCalculator.reducedCoinsForHalfDamage(baseCoins);
            }
        }

        String wonEquipment = wonCoins > 0 ? BossBattleCalculator.rollEquipment(random, halfReward) : null;

        if (defeated) {
            saveDefeatedBossCount(Math.max(readDefeatedBossCount(), data.bossIndex));
            clearActiveBoss();
        } else {
            saveActiveBoss(data.bossIndex, finalHp);
        }

        String endMessage = defeated
                ? "Bos je poražen! Protresi telefon da otvoriš kovčeg."
                : (wonCoins > 0 ? "Preživeo je, ali si osvojio umanjene nagrade." : "Bos je ostao živ. Bez nagrada.");

        state.setValue(new BossBattleUiState.Success(data.copy(
                finalHp,
                0,
                data.equipmentActivated,
                endMessage,
                true,
                wonCoins,
                wonEquipment,
                false
        )));
    }

    private void openChest() {
        BossBattleUiState current = state.getValue();
        if (current == null) return;
        BossBattleUiState.Data data = current.getData();
        if (!data.battleFinished || !data.battleAvailable) return;

        state.setValue(new BossBattleUiState.Success(data.copy(
                data.bossHp,
                data.attacksLeft,
                data.equipmentActivated,
                "Kovčeg je otvoren!",
                true,
                data.wonCoins,
                data.wonEquipment,
                true
        )));
    }

    private int readDefeatedBossCount() {
        if (uid == null || uid.isEmpty()) return 0;
        return Math.max(0, sharedPreferences.getInt(KEY_DEFEATED_BOSS_PREFIX + uid, 0));
    }

    private void saveDefeatedBossCount(int value) {
        if (uid == null || uid.isEmpty()) return;
        sharedPreferences.edit().putInt(KEY_DEFEATED_BOSS_PREFIX + uid, Math.max(0, value)).apply();
    }

    private int readActiveBossIndex(int fallback) {
        if (uid == null || uid.isEmpty()) return fallback;
        return Math.max(1, sharedPreferences.getInt(KEY_ACTIVE_BOSS_INDEX_PREFIX + uid, fallback));
    }

    private int readActiveBossHp(int fallback) {
        if (uid == null || uid.isEmpty()) return fallback;
        return Math.max(0, sharedPreferences.getInt(KEY_ACTIVE_BOSS_HP_PREFIX + uid, fallback));
    }

    private void saveActiveBoss(int bossIndex, int bossHp) {
        if (uid == null || uid.isEmpty()) return;
        sharedPreferences.edit()
                .putInt(KEY_ACTIVE_BOSS_INDEX_PREFIX + uid, Math.max(1, bossIndex))
                .putInt(KEY_ACTIVE_BOSS_HP_PREFIX + uid, Math.max(0, bossHp))
                .apply();
    }

    private void clearActiveBoss() {
        if (uid == null || uid.isEmpty()) return;
        sharedPreferences.edit()
                .remove(KEY_ACTIVE_BOSS_INDEX_PREFIX + uid)
                .remove(KEY_ACTIVE_BOSS_HP_PREFIX + uid)
                .apply();
    }
}
