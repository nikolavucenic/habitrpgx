package com.example.habitrpg.feature.progression;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.SavedStateHandle;

import com.example.domain.core.Result;
import com.example.domain.model.User;
import com.example.domain.progression.ProgressionCalculator;
import com.example.domain.usecase.GetCurrentUserProfileUseCase;
import com.example.habitrpg.core.CoreViewModel;

import java.util.Map;

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

        getCurrentUserProfileUseCase.execute().thenAccept(result ->
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (result instanceof Result.Success) {
                        state.setValue(new ProgressionUiState.Success(toData(((Result.Success<User>) result).data)));
                    } else if (result instanceof Result.Error) {
                        state.setValue(new ProgressionUiState.Error(currentData, ((Result.Error<User>) result).message));
                    }
                })
        );
    }

    private ProgressionUiState.Data toData(User user) {
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

    private String formatPreview(Map<String, Integer> values) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Integer> entry : values.entrySet()) {
            if (builder.length() > 0) builder.append("\n");
            builder.append("• ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" XP");
        }
        return builder.toString();
    }
}
