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

    private final GetCurrentUserProfileUseCase getCurrentUserProfileUseCase;
    private final SharedPreferences sharedPreferences;

    @Inject
    public ProgressionViewModel(GetCurrentUserProfileUseCase getCurrentUserProfileUseCase,
                                SharedPreferences sharedPreferences,
                                SavedStateHandle savedStateHandle) {
        this.getCurrentUserProfileUseCase = getCurrentUserProfileUseCase;
        this.sharedPreferences = sharedPreferences;
        state.setValue(new ProgressionUiState.Loading(ProgressionUiState.initialData()));
    }

    @Override
    public void handleAction(ProgressionAction action) {
        if (action instanceof ProgressionAction.Load) {
            load();
        }
    }

    private void load() {
        ProgressionUiState current = state.getValue();
        ProgressionUiState.Data currentData = current != null ? current.getData() : ProgressionUiState.initialData();
        state.setValue(new ProgressionUiState.Loading(currentData));

        getCurrentUserProfileUseCase.execute().thenAccept(profileResult ->
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (result instanceof Result.Success) {
                        state.setValue(new ProgressionUiState.Success(toData(((Result.Success<User>) result).data)));
                    } else if (result instanceof Result.Error) {
                        state.setValue(new ProgressionUiState.Error(currentData, ((Result.Error<User>) result).message));
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
        int defeatedBossCount = sharedPreferences.getInt(KEY_DEFEATED_BOSS_PREFIX + user.uid, 0);
        int pendingBossIndex = defeatedBossCount + 1;
        boolean hasBossEncounter = pendingBossIndex <= Math.max(1, user.level);

        return new ProgressionUiState.Data(
                user.username,
                ProgressionCalculator.titleForLevel(user.level),
                user.level,
                user.avatarId,
                Math.max(user.pp, 0),
                Math.max(user.xp, 0),
                requiredXp,
                formatPreview(ProgressionCalculator.importanceXpByPassedLevel(passedLevels)),
                formatPreview(ProgressionCalculator.difficultyXpByPassedLevel(passedLevels)),
                hasBossEncounter
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
