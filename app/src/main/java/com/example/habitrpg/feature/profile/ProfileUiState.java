package com.example.habitrpg.feature.profile;

import com.example.domain.model.User;

public class ProfileUiState {
    public final boolean loading;
    public final User user;
    public final String oldPassword;
    public final String newPassword;
    public final String confirmPassword;
    public final String oldPasswordError;
    public final String newPasswordError;
    public final String confirmPasswordError;
    public final String error;

    public ProfileUiState(boolean loading,
                          User user,
                          String oldPassword,
                          String newPassword,
                          String confirmPassword,
                          String oldPasswordError,
                          String newPasswordError,
                          String confirmPasswordError,
                          String error) {
        this.loading = loading;
        this.user = user;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
        this.oldPasswordError = oldPasswordError;
        this.newPasswordError = newPasswordError;
        this.confirmPasswordError = confirmPasswordError;
        this.error = error;
    }

    public static ProfileUiState initial() {
        return new ProfileUiState(true, null, "", "", "", null, null, null, null);
    }

    public ProfileUiState copy(boolean loading,
                               User user,
                               String oldPassword,
                               String newPassword,
                               String confirmPassword,
                               String oldPasswordError,
                               String newPasswordError,
                               String confirmPasswordError,
                               String error) {
        return new ProfileUiState(loading, user, oldPassword, newPassword, confirmPassword,
                oldPasswordError, newPasswordError, confirmPasswordError, error);
    }
}
