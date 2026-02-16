package com.example.habitrpg.feature.progression;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.SavedStateHandle;

import com.example.domain.core.Result;
import com.example.domain.model.TaskItem;
import com.example.domain.model.User;
import com.example.domain.progression.BossBattleCalculator;
import com.example.domain.progression.ProgressionCalculator;
import com.example.domain.usecase.GetCurrentUserProfileUseCase;
import com.example.domain.usecase.GetTasksUseCase;
import com.example.habitrpg.core.CoreViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ProgressionViewModel extends CoreViewModel<ProgressionUiState, ProgressionAction, Void> {

    private static final String KEY_DEFEATED_BOSS_PREFIX = "defeated_boss_";
    private static final String KEY_ACTIVE_BOSS_INDEX_PREFIX = "active_boss_index_";
    private static final String KEY_ACTIVE_BOSS_HP_PREFIX = "active_boss_hp_";

    private final GetCurrentUserProfileUseCase getCurrentUserProfileUseCase;
    private final GetTasksUseCase getTasksUseCase;
    private final SharedPreferences sharedPreferences;
    private final Random random = new Random();

    private String uid = "";

    @Inject
    public ProgressionViewModel(GetCurrentUserProfileUseCase getCurrentUserProfileUseCase,
                                GetTasksUseCase getTasksUseCase,
                                SharedPreferences sharedPreferences,
                                SavedStateHandle savedStateHandle) {
        this.getCurrentUserProfileUseCase = getCurrentUserProfileUseCase;
        this.getTasksUseCase = getTasksUseCase;
        this.sharedPreferences = sharedPreferences;
        state.setValue(new ProgressionUiState.Loading(ProgressionUiState.initialData()));
    }

    @Override
    public void handleAction(ProgressionAction action) {
        if (action instanceof ProgressionAction.Load) {
            load();
        } else if (action instanceof ProgressionAction.OnEquipmentToggle) {
            onEquipmentToggled(((ProgressionAction.OnEquipmentToggle) action).enabled);
        } else if (action instanceof ProgressionAction.OnAttackClicked || action instanceof ProgressionAction.OnShakeAttack) {
            performAttack();
        } else if (action instanceof ProgressionAction.OnOpenChest) {
            openChest();
        }
    }

    private void load() {
        ProgressionUiState current = state.getValue();
        ProgressionUiState.Data currentData = current != null ? current.getData() : ProgressionUiState.initialData();
        state.setValue(new ProgressionUiState.Loading(currentData));

        getCurrentUserProfileUseCase.execute().thenAccept(profileResult ->
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (profileResult instanceof Result.Error) {
                        state.setValue(new ProgressionUiState.Error(currentData, ((Result.Error<User>) profileResult).message));
                        return;
                    }

                    User user = ((Result.Success<User>) profileResult).data;
                    uid = user.uid;
                    getTasksUseCase.execute().thenAccept(tasksResult ->
                            new Handler(Looper.getMainLooper()).post(() -> {
                                List<TaskItem> tasks = new ArrayList<>();
                                if (tasksResult instanceof Result.Success) {
                                    tasks = ((Result.Success<List<TaskItem>>) tasksResult).data;
                                }
                                state.setValue(new ProgressionUiState.Success(toData(user, tasks)));
                            })
                    );
                })
        );
    }

    private ProgressionUiState.Data toData(User user, List<TaskItem> tasks) {
        int passedLevels = Math.max(0, user.level - 1);
        int requiredXp = ProgressionCalculator.requiredXpForLevel(user.level);
        int pp = Math.max(0, user.pp);
        int successRate = BossBattleCalculator.calculateSuccessRate(tasks);

        int defeatedBosses = readDefeatedBossCount();
        int pendingBossIndex = defeatedBosses + 1;
        boolean bossAvailable = pendingBossIndex <= Math.max(1, user.level);

        int bossIndex = Math.max(1, pendingBossIndex);
        int bossMaxHp = BossBattleCalculator.bossHpForIndex(bossIndex);
        int bossHp = bossMaxHp;

        if (bossAvailable) {
            int persistedIndex = readActiveBossIndex(bossIndex);
            int persistedHp = readActiveBossHp(bossMaxHp);
            if (persistedIndex == bossIndex) {
                bossHp = Math.max(0, Math.min(bossMaxHp, persistedHp));
            }
        }

        boolean hasEquipment = user.equipment != null && !user.equipment.isEmpty();
        String equippedItem = hasEquipment ? user.equipment.get(0) : "Bez opreme";

        return new ProgressionUiState.Data(
                user.username,
                ProgressionCalculator.titleForLevel(user.level),
                user.level,
                user.avatarId,
                pp,
                Math.max(user.xp, 0),
                requiredXp,
                formatPreview(ProgressionCalculator.importanceXpByPassedLevel(passedLevels)),
                formatPreview(ProgressionCalculator.difficultyXpByPassedLevel(passedLevels)),
                bossIndex,
                bossHp,
                bossMaxHp,
                BossBattleCalculator.ATTEMPTS_PER_BATTLE,
                successRate,
                false,
                pp,
                equippedItem,
                bossAvailable ? "Spreman za borbu!" : "Nema aktivne borbe. Pređi sledeći nivo da otključaš novog bosa.",
                !bossAvailable,
                false,
                0,
                null,
                false,
                bossAvailable
        );
    }

    private void onEquipmentToggled(boolean enabled) {
        ProgressionUiState current = state.getValue();
        if (current == null) return;
        ProgressionUiState.Data data = current.getData();
        if (data.battleFinished || !data.bossAvailable) return;

        boolean canActivate = !"Bez opreme".equals(data.equippedItem);
        boolean activate = enabled && canActivate;

        ProgressionUiState.Data updated = data.copyWithBattle(
                data.bossHp,
                data.attacksLeft,
                data.pp,
                activate,
                activate ? "Oprema aktivirana za ovu borbu." : "Borba bez aktivirane opreme.",
                false,
                false,
                0,
                null,
                false,
                true
        );
        state.setValue(new ProgressionUiState.Success(updated));
    }

    private void performAttack() {
        ProgressionUiState current = state.getValue();
        if (current == null) return;
        ProgressionUiState.Data data = current.getData();

        if (!data.bossAvailable || data.attacksLeft <= 0 || data.battleFinished) return;

        boolean hit = BossBattleCalculator.isAttackSuccessful(data.successRate, random);
        int newBossHp = data.bossHp;
        String message;

        if (hit) {
            newBossHp = Math.max(0, data.bossHp - Math.max(1, data.effectivePp));
            message = "Pogodak! Bos gubi " + Math.max(1, data.effectivePp) + " HP.";
        } else {
            message = "Promašaj! Pokušaj ponovo.";
        }

        int newAttacksLeft = data.attacksLeft - 1;
        boolean finished = newAttacksLeft == 0 || newBossHp == 0;

        ProgressionUiState.Data next = data.copyWithBattle(
                newBossHp,
                newAttacksLeft,
                data.effectivePp,
                data.equipmentActivated,
                message,
                finished,
                false,
                0,
                null,
                false,
                true
        );

        if (!finished) {
            saveActiveBoss(data.bossIndex, newBossHp);
            state.setValue(new ProgressionUiState.Success(next));
            return;
        }

        state.setValue(new ProgressionUiState.Success(resolveRewards(next)));
    }

    private ProgressionUiState.Data resolveRewards(ProgressionUiState.Data data) {
        boolean defeated = data.bossHp == 0;
        int baseCoins = BossBattleCalculator.coinsForBossIndex(data.bossIndex);
        int wonCoins = 0;
        String wonEquipment = null;
        boolean halfReward = false;

        if (defeated) {
            wonCoins = baseCoins;
        } else {
            int damage = data.bossMaxHp - data.bossHp;
            if (damage >= data.bossMaxHp / 2) {
                halfReward = true;
                wonCoins = BossBattleCalculator.reducedCoinsForHalfDamage(baseCoins);
            }
        }

        if (wonCoins > 0) {
            wonEquipment = BossBattleCalculator.rollEquipment(random, halfReward);
        }

        if (defeated) {
            saveDefeatedBossCount(Math.max(readDefeatedBossCount(), data.bossIndex));
            clearActiveBoss();
        } else {
            saveActiveBoss(data.bossIndex, data.bossHp);
        }

        String endMessage;
        if (defeated) {
            endMessage = "Bos je poražen! Protresi telefon da otvoriš kovčeg.";
        } else if (wonCoins > 0) {
            endMessage = "Bos nije poražen, ali si umanjio 50% HP-a i osvojio umanjene nagrade.";
        } else {
            endMessage = "Bos nije poražen. Bez nagrada u ovoj borbi.";
        }

        return data.copyWithBattle(
                data.bossHp,
                0,
                data.effectivePp,
                data.equipmentActivated,
                endMessage,
                true,
                defeated,
                wonCoins,
                wonEquipment,
                false,
                true
        );
    }

    private void openChest() {
        ProgressionUiState current = state.getValue();
        if (current == null) return;
        ProgressionUiState.Data data = current.getData();
        if (!data.battleFinished || !data.bossAvailable) return;

        ProgressionUiState.Data updated = data.copyWithBattle(
                data.bossHp,
                data.attacksLeft,
                data.effectivePp,
                data.equipmentActivated,
                "Kovčeg je otvoren!",
                true,
                data.bossDefeated,
                data.wonCoins,
                data.wonEquipment,
                true,
                true
        );
        state.setValue(new ProgressionUiState.Success(updated));
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

    private String formatPreview(Map<String, Integer> values) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Integer> entry : values.entrySet()) {
            if (builder.length() > 0) builder.append("\n");
            builder.append("• ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" XP");
        }
        return builder.toString();
    }
}
