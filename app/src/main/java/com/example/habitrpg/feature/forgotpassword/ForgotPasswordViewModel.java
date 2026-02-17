package com.example.habitrpg.feature.forgotpassword;

import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;

import androidx.lifecycle.SavedStateHandle;

import com.example.domain.core.Result;
import com.example.domain.usecase.RequestPasswordResetUseCase;
import com.example.habitrpg.core.CoreViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ForgotPasswordViewModel extends CoreViewModel<ForgotPasswordUiState, ForgotPasswordAction, ForgotPasswordSideEffect> {

    private final RequestPasswordResetUseCase requestPasswordResetUseCase;

    @Inject
    public ForgotPasswordViewModel(RequestPasswordResetUseCase requestPasswordResetUseCase, SavedStateHandle savedStateHandle) {
        this.requestPasswordResetUseCase = requestPasswordResetUseCase;
        state.setValue(new ForgotPasswordUiState.Input("", null));
    }

    @Override
    public void handleAction(ForgotPasswordAction action) {
        ForgotPasswordUiState current = state.getValue();
        if (current == null) return;

        if (action instanceof ForgotPasswordAction.OnEmailChanged) {
            state.setValue(new ForgotPasswordUiState.Input(((ForgotPasswordAction.OnEmailChanged) action).email, null));
        } else if (action instanceof ForgotPasswordAction.OnSendResetEmailClicked) {
            performReset(current.getEmail());
        } else if (action instanceof ForgotPasswordAction.OnBackToLoginClicked) {
            sideEffect.setValue(new ForgotPasswordSideEffect.NavigateBackToLogin());
        }
    }

    private void performReset(String rawEmail) {
        String email = rawEmail == null ? "" : rawEmail.trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            state.setValue(new ForgotPasswordUiState.Input(email, "Unesite ispravnu email adresu."));
            return;
        }

        state.setValue(new ForgotPasswordUiState.Loading(email));

        requestPasswordResetUseCase.execute(email)
                .thenAccept(result -> new Handler(Looper.getMainLooper()).post(() -> {
                    state.setValue(new ForgotPasswordUiState.Input(email, null));
                    if (result instanceof Result.Success) {
                        sideEffect.setValue(new ForgotPasswordSideEffect.ShowToast("Poslali smo email za reset lozinke."));
                        sideEffect.setValue(new ForgotPasswordSideEffect.NavigateBackToLogin());
                    } else if (result instanceof Result.Error) {
                        String message = ((Result.Error<Void>) result).message;
                        sideEffect.setValue(new ForgotPasswordSideEffect.ShowToast(mapForgotPasswordError(message)));
                    }
                }));
    }

    private String mapForgotPasswordError(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "Greška pri slanju email-a za reset lozinke.";
        }

        String lower = message.toLowerCase();
        if (lower.contains("badly formatted") || lower.contains("invalid-email")) {
            return "Email adresa nije u dobrom formatu.";
        }
        if (lower.contains("too-many-requests")) {
            return "Previše pokušaja. Pokušajte ponovo malo kasnije.";
        }
        if (lower.contains("network")) {
            return "Proverite internet konekciju i pokušajte ponovo.";
        }
        return "Nismo uspeli da pošaljemo email za reset lozinke.";
    }
}
