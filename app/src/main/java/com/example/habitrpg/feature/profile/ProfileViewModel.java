package com.example.habitrpg.feature.profile;

import android.os.Handler;
import android.os.Looper;

import com.example.domain.core.Result;
import com.example.domain.model.User;
import com.example.domain.usecase.ChangePasswordUseCase;
import com.example.domain.usecase.GetCurrentUserProfileUseCase;
import com.example.domain.usecase.LogoutUseCase;
import com.example.habitrpg.core.CoreViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ProfileViewModel extends CoreViewModel<ProfileUiState, ProfileAction, ProfileSideEffect> {

    private final GetCurrentUserProfileUseCase getCurrentUserProfileUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final LogoutUseCase logoutUseCase;

    @Inject
    public ProfileViewModel(GetCurrentUserProfileUseCase getCurrentUserProfileUseCase,
                            ChangePasswordUseCase changePasswordUseCase,
                            LogoutUseCase logoutUseCase) {
        this.getCurrentUserProfileUseCase = getCurrentUserProfileUseCase;
        this.changePasswordUseCase = changePasswordUseCase;
        this.logoutUseCase = logoutUseCase;
        state.setValue(new ProfileUiState.Loading(null, "", "", ""));
    }

    @Override
    public void handleAction(ProfileAction action) {
        ProfileUiState current = state.getValue();
        if (current == null) return;

        if (action instanceof ProfileAction.LoadProfile) {
            loadProfile(current);
        } else if (action instanceof ProfileAction.OnOldPasswordChanged) {
            state.setValue(new ProfileUiState.Input(
                    current.getUser(),
                    ((ProfileAction.OnOldPasswordChanged) action).value,
                    current.getNewPassword(),
                    current.getConfirmPassword(),
                    null,
                    null,
                    null
            ));
        } else if (action instanceof ProfileAction.OnNewPasswordChanged) {
            state.setValue(new ProfileUiState.Input(
                    current.getUser(),
                    current.getOldPassword(),
                    ((ProfileAction.OnNewPasswordChanged) action).value,
                    current.getConfirmPassword(),
                    null,
                    null,
                    null
            ));
        } else if (action instanceof ProfileAction.OnConfirmPasswordChanged) {
            state.setValue(new ProfileUiState.Input(
                    current.getUser(),
                    current.getOldPassword(),
                    current.getNewPassword(),
                    ((ProfileAction.OnConfirmPasswordChanged) action).value,
                    null,
                    null,
                    null
            ));
        } else if (action instanceof ProfileAction.OnChangePasswordClicked) {
            submitChangePassword(current);
        } else if (action instanceof ProfileAction.OnLogoutClicked) {
            logout();
        }
    }

    private void loadProfile(ProfileUiState current) {
        state.setValue(new ProfileUiState.Loading(
                current.getUser(),
                current.getOldPassword(),
                current.getNewPassword(),
                current.getConfirmPassword()
        ));

        getCurrentUserProfileUseCase.execute().thenAccept(result ->
                new Handler(Looper.getMainLooper()).post(() -> {
                    ProfileUiState latest = state.getValue();
                    if (latest == null) return;

                    if (result instanceof Result.Success) {
                        state.setValue(new ProfileUiState.Input(
                                ((Result.Success<User>) result).data,
                                latest.getOldPassword(),
                                latest.getNewPassword(),
                                latest.getConfirmPassword(),
                                null,
                                null,
                                null
                        ));
                    } else if (result instanceof Result.Error) {
                        state.setValue(new ProfileUiState.Error(
                                latest.getUser(),
                                latest.getOldPassword(),
                                latest.getNewPassword(),
                                latest.getConfirmPassword(),
                                ((Result.Error<User>) result).message
                        ));
                    }
                }));
    }

    private void submitChangePassword(ProfileUiState current) {
        String oldPassword = current.getOldPassword().trim();
        String newPassword = current.getNewPassword().trim();
        String confirmPassword = current.getConfirmPassword().trim();

        String oldError = oldPassword.isEmpty() ? "Unesite staru lozinku" : null;
        String newError = newPassword.isEmpty() ? "Unesite novu lozinku" : null;
        String confirmError = confirmPassword.isEmpty() ? "Potvrdite novu lozinku" : null;

        if (confirmError == null && !newPassword.equals(confirmPassword)) {
            confirmError = "Lozinke se ne poklapaju";
        }

        if (newError == null && newPassword.length() < 6) {
            newError = "Nova lozinka mora imati bar 6 karaktera";
        }

        if (oldError != null || newError != null || confirmError != null) {
            state.setValue(new ProfileUiState.Input(
                    current.getUser(),
                    current.getOldPassword(),
                    current.getNewPassword(),
                    current.getConfirmPassword(),
                    oldError,
                    newError,
                    confirmError
            ));
            return;
        }

        state.setValue(new ProfileUiState.Loading(
                current.getUser(),
                current.getOldPassword(),
                current.getNewPassword(),
                current.getConfirmPassword()
        ));

        changePasswordUseCase.execute(oldPassword, newPassword).thenAccept(result ->
                new Handler(Looper.getMainLooper()).post(() -> {
                    ProfileUiState latest = state.getValue();
                    if (latest == null) return;

                    if (result instanceof Result.Success) {
                        state.setValue(new ProfileUiState.Input(
                                latest.getUser(),
                                "",
                                "",
                                "",
                                null,
                                null,
                                null
                        ));
                        sideEffect.setValue(new ProfileSideEffect.ShowToast("Lozinka uspešno promenjena."));
                    } else if (result instanceof Result.Error) {
                        String message = ((Result.Error<Void>) result).message;
                        state.setValue(new ProfileUiState.Error(
                                latest.getUser(),
                                latest.getOldPassword(),
                                latest.getNewPassword(),
                                latest.getConfirmPassword(),
                                message
                        ));
                        sideEffect.setValue(new ProfileSideEffect.ShowToast(message));
                    }
                }));
    }

    private void logout() {
        logoutUseCase.execute().thenAccept(result ->
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (result instanceof Result.Success) {
                        sideEffect.setValue(new ProfileSideEffect.NavigateToLogin());
                    } else if (result instanceof Result.Error) {
                        sideEffect.setValue(new ProfileSideEffect.ShowToast(((Result.Error<Void>) result).message));
                    }
                }));
    }
}
