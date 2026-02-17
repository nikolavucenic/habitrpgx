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
    private final MutableLiveData<ProgressionUiState> state =
            new MutableLiveData<>(new ProgressionUiState.Loading(ProgressionUiState.initialData()));

    @Inject
    public ProgressionViewModel(GetCurrentUserProfileUseCase getCurrentUserProfileUseCase) {
        this.getCurrentUserProfileUseCase = getCurrentUserProfileUseCase;
    }

    public LiveData<ProgressionUiState> getState() {
        return state;
    }

    public void load() {
        ProgressionUiState current = state.getValue();
        ProgressionUiState.Data currentData = current != null ? current.getData() : ProgressionUiState.initialData();
        state.setValue(new ProgressionUiState.Loading(currentData));

        getCurrentUserProfileUseCase.execute().thenAccept(result ->
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (result instanceof Result.Success) {
                        state.setValue(new ProgressionUiState.Success(toData(((Result.Success<User>) result).data)));
                    } else if (result instanceof Result.Error) {
                        ProgressionUiState.Data fallback = currentData;
                        state.setValue(new ProgressionUiState.Error(fallback, ((Result.Error<User>) result).message));
                    }
                })
        );
    }

    private ProgressionUiState.Data toData(User user) {
        int passedLevels = Math.max(0, user.level - 1);
        int requiredXp = ProgressionCalculator.requiredXpForLevel(user.level);
        int pp = Math.max(user.pp, 0);

        return new ProgressionUiState.Data(
                user.username,
                ProgressionCalculator.titleForLevel(user.level),
                user.level,
                user.avatarId,
                pp,
                Math.max(user.xp, 0),
                requiredXp,
                user.level > 1,
                formatPreview(ProgressionCalculator.importanceXpByPassedLevel(passedLevels)),
                formatPreview(ProgressionCalculator.difficultyXpByPassedLevel(passedLevels))
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
