package com.example.habitrpg.feature.login;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.SavedStateHandle;

import com.example.domain.core.Result;
import com.example.domain.model.User;
import com.example.domain.usecase.LoginUseCase;
import com.example.habitrpg.core.CoreViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginViewModel extends CoreViewModel<LoginUiState, LoginAction, LoginSideEffect> {

    private final LoginUseCase loginUseCase;

    @Inject
    public LoginViewModel(LoginUseCase loginUseCase, SavedStateHandle savedStateHandle) {
        this.loginUseCase = loginUseCase;
        state.setValue(new LoginUiState.Input("", "", null, null));
    }

    @Override
    public void handleAction(LoginAction action) {
        LoginUiState current = state.getValue();
        if (current == null) return;

        if (action instanceof LoginAction.OnEmailChanged) {
            state.setValue(new LoginUiState.Input(((LoginAction.OnEmailChanged) action).email, current.getPassword(), null, null));
        } else if (action instanceof LoginAction.OnPasswordChanged) {
            state.setValue(new LoginUiState.Input(current.getEmail(), ((LoginAction.OnPasswordChanged) action).password, null, null));
        } else if (action instanceof LoginAction.OnLoginClicked) {
            performLogin(current);
        } else if (action instanceof LoginAction.OnGoToRegisterClicked) {
            sideEffect.setValue(new LoginSideEffect.NavigateToRegister());
        }
    }

    private void performLogin(LoginUiState current) {
        String email = current.getEmail().trim();
        String password = current.getPassword().trim();

        if (email.isEmpty() || password.isEmpty()) {
            state.setValue(new LoginUiState.Input(
                    email,
                    password,
                    email.isEmpty() ? "Email je obavezan" : null,
                    password.isEmpty() ? "Lozinka je obavezna" : null
            ));
            return;
        }

        state.setValue(new LoginUiState.Loading(email, password));

        loginUseCase.execute(current.getEmail(), current.getPassword())
                .thenAccept(result -> {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (result instanceof Result.Success) {
                            sideEffect.setValue(new LoginSideEffect.NavigateToHome());
                        } else if (result instanceof Result.Error) {
                            String msg = ((Result.Error<User>) result).message;
                            state.setValue(new LoginUiState.Error(current.getEmail(), current.getPassword(), null, null));
                            sideEffect.setValue(new LoginSideEffect.ShowToast(msg));
                        }
                    });
                });
    }
}
