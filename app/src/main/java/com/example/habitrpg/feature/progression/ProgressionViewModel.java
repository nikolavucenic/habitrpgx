package com.example.habitrpg.feature.progression;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.domain.core.Result;
import com.example.domain.model.User;
import com.example.domain.progression.ProgressionCalculator;
import com.example.domain.usecase.GetCurrentUserProfileUseCase;

import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ProgressionViewModel extends ViewModel {

    private final GetCurrentUserProfileUseCase getCurrentUserProfileUseCase;
    private final MutableLiveData<ProgressionUiState> state = new MutableLiveData<>(ProgressionUiState.initial());

    @Inject
    public ProgressionViewModel(GetCurrentUserProfileUseCase getCurrentUserProfileUseCase) {
        this.getCurrentUserProfileUseCase = getCurrentUserProfileUseCase;
    }

    public LiveData<ProgressionUiState> getState() {
        return state;
    }

    public void load() {
        getCurrentUserProfileUseCase.execute().thenAccept(result ->
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (result instanceof Result.Success) {
                        bindUser(((Result.Success<User>) result).data);
                    } else if (result instanceof Result.Error) {
                        ProgressionUiState fallback = ProgressionUiState.initial();
                        state.setValue(new ProgressionUiState(
                                fallback.username,
                                fallback.title,
                                fallback.level,
                                fallback.pp,
                                fallback.currentXp,
                                fallback.requiredXp,
                                formatPreview(ProgressionCalculator.importanceXpByPassedLevel(0)),
                                formatPreview(ProgressionCalculator.difficultyXpByPassedLevel(0)),
                                ((Result.Error<User>) result).message
                        ));
                    }
                })
        );
    }

    private void bindUser(User user) {
        int passedLevels = Math.max(0, user.level - 1);
        int requiredXp = ProgressionCalculator.requiredXpForLevel(user.level);
        int derivedPp = user.level > 1 ? ProgressionCalculator.ppRewardForLevel(user.level - 1) : 0;
        int pp = user.pp > 0 ? user.pp : derivedPp;

        state.setValue(new ProgressionUiState(
                user.username,
                ProgressionCalculator.titleForLevel(user.level),
                user.level,
                pp,
                Math.max(user.xp, 0),
                requiredXp,
                formatPreview(ProgressionCalculator.importanceXpByPassedLevel(passedLevels)),
                formatPreview(ProgressionCalculator.difficultyXpByPassedLevel(passedLevels)),
                null
        ));
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
