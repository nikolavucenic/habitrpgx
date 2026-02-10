package com.example.habitrpg.presentation.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.domain.core.Result;
import com.example.domain.model.ActivationToken;
import com.example.domain.usecase.ActivateAccountUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ActivationHandlerViewModel extends ViewModel {

    private final ActivateAccountUseCase activateAccountUseCase;
    private final MutableLiveData<ActivationUiState> uiState = new MutableLiveData<>(ActivationUiState.idle());

    @Inject
    public ActivationHandlerViewModel(ActivateAccountUseCase activateAccountUseCase) {
        this.activateAccountUseCase = activateAccountUseCase;
    }

    public LiveData<ActivationUiState> getUiState() {
        return uiState;
    }

    public void activate(String token) {
        if (token == null || token.trim().isEmpty()) {
            uiState.setValue(ActivationUiState.error("Missing activation token."));
            return;
        }

        ActivationToken activationToken = new ActivationToken(token, "", System.currentTimeMillis());
        Result<Void> result = activateAccountUseCase.execute(activationToken, System.currentTimeMillis());

        if (result instanceof Result.Error) {
            uiState.setValue(ActivationUiState.error(((Result.Error<Void>) result).message));
            return;
        }

        uiState.setValue(ActivationUiState.success());
    }

    public static class ActivationUiState {
        public final boolean success;
        public final String errorMessage;

        private ActivationUiState(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public static ActivationUiState idle() {
            return new ActivationUiState(false, null);
        }

        public static ActivationUiState success() {
            return new ActivationUiState(true, null);
        }

        public static ActivationUiState error(String message) {
            return new ActivationUiState(false, message);
        }
    }
}
