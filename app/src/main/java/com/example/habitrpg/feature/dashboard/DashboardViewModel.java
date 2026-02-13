package com.example.habitrpg.feature.dashboard;

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
public class DashboardViewModel extends ViewModel {

    private final GetCurrentUserProfileUseCase getCurrentUserProfileUseCase;
    private final MutableLiveData<DashboardUiState> state = new MutableLiveData<>(DashboardUiState.loading());

    @Inject
    public DashboardViewModel(GetCurrentUserProfileUseCase getCurrentUserProfileUseCase) {
        this.getCurrentUserProfileUseCase = getCurrentUserProfileUseCase;
    }

    public LiveData<DashboardUiState> getState() {
        return state;
    }

    public void loadProgression() {
        state.setValue(DashboardUiState.loading());

        getCurrentUserProfileUseCase.execute().thenAccept(result ->
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (result instanceof Result.Success) {
                        User user = ((Result.Success<User>) result).data;
                        bindUser(user);
                    } else if (result instanceof Result.Error) {
                        String message = ((Result.Error<User>) result).message;
                        state.setValue(new DashboardUiState(
                                false,
                                "Heroj",
                                ProgressionCalculator.titleForLevel(1),
                                1,
                                0,
                                0,
                                ProgressionCalculator.requiredXpForLevel(1),
                                formatPreview(ProgressionCalculator.importanceXpByPassedLevel(0)),
                                formatPreview(ProgressionCalculator.difficultyXpByPassedLevel(0)),
                                message
                        ));
                    }
                })
        );
    }

    private void bindUser(User user) {
        int passedLevels = Math.max(0, user.level - 1);
        int requiredXp = ProgressionCalculator.requiredXpForLevel(user.level);
        int pp = user.pp > 0 ? user.pp : ProgressionCalculator.ppRewardForLevel(passedLevels);

        state.setValue(new DashboardUiState(
                false,
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
