package com.example.habitrpg.feature;

import android.os.Handler;
import android.os.Looper;

import com.example.domain.core.Result;
import com.example.domain.model.User;
import com.example.domain.usecase.LoginUseCase;
import com.example.habitrpg.core.CoreViewModel;

public class LoginViewModel extends CoreViewModel<LoginUiState, LoginAction, LoginSideEffect> {

    private final LoginUseCase loginUseCase;

    public LoginViewModel(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
        state.setValue(new LoginUiState.Input("", "", null));
    }

    @Override
    public void handleAction(LoginAction action) {
        LoginUiState current = state.getValue();
        if (current == null) return;

        if (action instanceof LoginAction.OnEmailChanged) {
            state.setValue(new LoginUiState.Input(((LoginAction.OnEmailChanged) action).email, current.getPassword(), null));
        } else if (action instanceof LoginAction.OnPasswordChanged) {
            state.setValue(new LoginUiState.Input(current.getEmail(), ((LoginAction.OnPasswordChanged) action).password, null));
        } else if (action instanceof LoginAction.OnLoginClicked) {
            performLogin(current);
        } else if (action instanceof LoginAction.OnGoToRegisterClicked) {
            sideEffect.setValue(new LoginSideEffect.NavigateToRegister());
        }
    }

    private void performLogin(LoginUiState current) {
        state.setValue(new LoginUiState.Loading(current.getEmail(), current.getPassword()));

        loginUseCase.execute(current.getEmail(), current.getPassword())
                .thenAccept(result -> {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (result instanceof Result.Success) {
                            sideEffect.setValue(new LoginSideEffect.NavigateToHome());
                        } else if (result instanceof Result.Error) {
                            String msg = ((Result.Error<User>) result).message;
                            state.setValue(new LoginUiState.Error(current.getEmail(), current.getPassword(), msg));
                            sideEffect.setValue(new LoginSideEffect.ShowToast(msg));
                        }
                    });
                });
    }
}