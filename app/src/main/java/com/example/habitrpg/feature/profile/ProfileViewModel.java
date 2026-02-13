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
        state.setValue(ProfileUiState.initial());
    }

    @Override
    public void handleAction(ProfileAction action) {
        ProfileUiState current = state.getValue();
        if (current == null) return;

        if (action instanceof ProfileAction.LoadProfile) {
            loadProfile(current);
        } else if (action instanceof ProfileAction.OnOldPasswordChanged) {
            state.setValue(current.copy(false, current.user,
                    ((ProfileAction.OnOldPasswordChanged) action).value,
                    current.newPassword,
                    current.confirmPassword,
                    null, current.newPasswordError, current.confirmPasswordError, current.error));
        } else if (action instanceof ProfileAction.OnNewPasswordChanged) {
            state.setValue(current.copy(false, current.user,
                    current.oldPassword,
                    ((ProfileAction.OnNewPasswordChanged) action).value,
                    current.confirmPassword,
                    current.oldPasswordError, null, current.confirmPasswordError, current.error));
        } else if (action instanceof ProfileAction.OnConfirmPasswordChanged) {
            state.setValue(current.copy(false, current.user,
                    current.oldPassword,
                    current.newPassword,
                    ((ProfileAction.OnConfirmPasswordChanged) action).value,
                    current.oldPasswordError, current.newPasswordError, null, current.error));
        } else if (action instanceof ProfileAction.OnChangePasswordClicked) {
            submitChangePassword(current);
        } else if (action instanceof ProfileAction.OnLogoutClicked) {
            logout();
        }
    }

    private void loadProfile(ProfileUiState current) {
        state.setValue(current.copy(true, current.user,
                current.oldPassword, current.newPassword, current.confirmPassword,
                current.oldPasswordError, current.newPasswordError, current.confirmPasswordError, null));

        getCurrentUserProfileUseCase.execute().thenAccept(result ->
                new Handler(Looper.getMainLooper()).post(() -> {
                    ProfileUiState latest = state.getValue();
                    if (latest == null) return;

                    if (result instanceof Result.Success) {
                        state.setValue(latest.copy(false, ((Result.Success<User>) result).data,
                                latest.oldPassword, latest.newPassword, latest.confirmPassword,
                                null, null, null, null));
                    } else if (result instanceof Result.Error) {
                        state.setValue(latest.copy(false, latest.user,
                                latest.oldPassword, latest.newPassword, latest.confirmPassword,
                                null, null, null, ((Result.Error<User>) result).message));
                    }
                }));
    }

    private void submitChangePassword(ProfileUiState current) {
        String oldPassword = current.oldPassword.trim();
        String newPassword = current.newPassword.trim();
        String confirmPassword = current.confirmPassword.trim();

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
            state.setValue(current.copy(false, current.user,
                    current.oldPassword, current.newPassword, current.confirmPassword,
                    oldError, newError, confirmError, current.error));
            return;
        }

        state.setValue(current.copy(true, current.user,
                current.oldPassword, current.newPassword, current.confirmPassword,
                null, null, null, null));

        changePasswordUseCase.execute(oldPassword, newPassword).thenAccept(result ->
                new Handler(Looper.getMainLooper()).post(() -> {
                    ProfileUiState latest = state.getValue();
                    if (latest == null) return;

                    if (result instanceof Result.Success) {
                        state.setValue(latest.copy(false, latest.user,
                                "", "", "", null, null, null, null));
                        sideEffect.setValue(new ProfileSideEffect.ShowToast("Lozinka uspešno promenjena."));
                    } else if (result instanceof Result.Error) {
                        state.setValue(latest.copy(false, latest.user,
                                latest.oldPassword, latest.newPassword, latest.confirmPassword,
                                null, null, null, ((Result.Error<Void>) result).message));
                        sideEffect.setValue(new ProfileSideEffect.ShowToast(((Result.Error<Void>) result).message));
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
