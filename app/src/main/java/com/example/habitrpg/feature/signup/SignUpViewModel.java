package com.example.habitrpg.feature.signup;

import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;

import androidx.lifecycle.SavedStateHandle;

import com.example.domain.core.Result;
import com.example.domain.usecase.RegisterUseCase;
import com.example.habitrpg.core.CoreViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SignUpViewModel extends CoreViewModel<SignUpUiState, SignUpAction, SignUpSideEffect> {

    private final RegisterUseCase registerUseCase;

    @Inject
    public SignUpViewModel(RegisterUseCase registerUseCase, SavedStateHandle savedStateHandle) {
        this.registerUseCase = registerUseCase;
        state.setValue(inputState("", "", "", "", 1, null));
    }

    @Override
    public void handleAction(SignUpAction action) {
        SignUpUiState current = state.getValue();
        if (current == null) return;

        if (action instanceof SignUpAction.OnUsernameChanged) {
            state.setValue(inputState(((SignUpAction.OnUsernameChanged) action).username, current.getEmail(), current.getPassword(), current.getConfirmPassword(), current.getAvatarId(), null));
        } else if (action instanceof SignUpAction.OnEmailChanged) {
            state.setValue(inputState(current.getUsername(), ((SignUpAction.OnEmailChanged) action).email, current.getPassword(), current.getConfirmPassword(), current.getAvatarId(), null));
        } else if (action instanceof SignUpAction.OnPasswordChanged) {
            state.setValue(inputState(current.getUsername(), current.getEmail(), ((SignUpAction.OnPasswordChanged) action).password, current.getConfirmPassword(), current.getAvatarId(), null));
        } else if (action instanceof SignUpAction.OnConfirmPasswordChanged) {
            state.setValue(inputState(current.getUsername(), current.getEmail(), current.getPassword(), ((SignUpAction.OnConfirmPasswordChanged) action).confirmPassword, current.getAvatarId(), null));
        } else if (action instanceof SignUpAction.OnAvatarSelected) {
            state.setValue(inputState(current.getUsername(), current.getEmail(), current.getPassword(), current.getConfirmPassword(), ((SignUpAction.OnAvatarSelected) action).avatarId, null));
        } else if (action instanceof SignUpAction.OnBackToLoginClicked) {
            sideEffect.setValue(new SignUpSideEffect.NavigateToLogin());
        } else if (action instanceof SignUpAction.OnRegisterClicked) {
            performRegister(current);
        }
    }

    private void performRegister(SignUpUiState current) {
        ValidationError validationError = validate(current);
        if (validationError != null) {
            state.setValue(inputState(current.getUsername(), current.getEmail(), current.getPassword(), current.getConfirmPassword(), current.getAvatarId(), validationError));
            return;
        }

        state.setValue(new SignUpUiState.Loading(current.getUsername(), current.getEmail(), current.getPassword(), current.getConfirmPassword(), current.getAvatarId()));

        registerUseCase.execute(current.getEmail(), current.getPassword(), current.getUsername(), current.getAvatarId())
                .thenAccept(result -> new Handler(Looper.getMainLooper()).post(() -> {
                    if (result instanceof Result.Success) {
                        sideEffect.setValue(new SignUpSideEffect.ShowToast("Uspešna registracija. Aktivirajte nalog preko email linka (24h)."));
                        sideEffect.setValue(new SignUpSideEffect.NavigateToLogin());
                    } else if (result instanceof Result.Error) {
                        String msg = ((Result.Error<Void>) result).message;
                        state.setValue(inputState(current.getUsername(), current.getEmail(), current.getPassword(), current.getConfirmPassword(), current.getAvatarId(), null));
                        sideEffect.setValue(new SignUpSideEffect.ShowToast(msg));
                    }
                }));
    }

    private ValidationError validate(SignUpUiState current) {
        if (current.getUsername().trim().length() < 3) return new ValidationError(Field.USERNAME, "Korisničko ime mora imati bar 3 karaktera.");
        if (!Patterns.EMAIL_ADDRESS.matcher(current.getEmail()).matches()) return new ValidationError(Field.EMAIL, "Email nije u dobrom formatu.");
        if (current.getPassword().length() < 8) return new ValidationError(Field.PASSWORD, "Lozinka mora imati bar 8 karaktera.");
        if (!current.getPassword().equals(current.getConfirmPassword())) return new ValidationError(Field.CONFIRM_PASSWORD, "Lozinke se ne podudaraju.");
        return null;
    }

    private SignUpUiState.Input inputState(String username, String email, String password, String confirmPassword, int avatarId, ValidationError error) {
        return new SignUpUiState.Input(
                username,
                email,
                password,
                confirmPassword,
                avatarId,
                error != null && error.field == Field.USERNAME ? error.message : null,
                error != null && error.field == Field.EMAIL ? error.message : null,
                error != null && error.field == Field.PASSWORD ? error.message : null,
                error != null && error.field == Field.CONFIRM_PASSWORD ? error.message : null
        );
    }

    private enum Field {
        USERNAME,
        EMAIL,
        PASSWORD,
        CONFIRM_PASSWORD
    }

    private static class ValidationError {
        private final Field field;
        private final String message;

        private ValidationError(Field field, String message) {
            this.field = field;
            this.message = message;
        }
    }
}
