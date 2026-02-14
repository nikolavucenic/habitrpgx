package com.example.habitrpg.feature.profile;

import com.example.domain.model.User;

public interface ProfileUiState {
    User getUser();
    String getOldPassword();
    String getNewPassword();
    String getConfirmPassword();

    class Input implements ProfileUiState {
        private final User user;
        private final String oldPassword;
        private final String newPassword;
        private final String confirmPassword;
        private final String oldPasswordError;
        private final String newPasswordError;
        private final String confirmPasswordError;

        public Input(User user,
                     String oldPassword,
                     String newPassword,
                     String confirmPassword,
                     String oldPasswordError,
                     String newPasswordError,
                     String confirmPasswordError) {
            this.user = user;
            this.oldPassword = oldPassword;
            this.newPassword = newPassword;
            this.confirmPassword = confirmPassword;
            this.oldPasswordError = oldPasswordError;
            this.newPasswordError = newPasswordError;
            this.confirmPasswordError = confirmPasswordError;
        }

        @Override
        public User getUser() {
            return user;
        }

        @Override
        public String getOldPassword() {
            return oldPassword;
        }

        @Override
        public String getNewPassword() {
            return newPassword;
        }

        @Override
        public String getConfirmPassword() {
            return confirmPassword;
        }

        public String getOldPasswordError() {
            return oldPasswordError;
        }

        public String getNewPasswordError() {
            return newPasswordError;
        }

        public String getConfirmPasswordError() {
            return confirmPasswordError;
        }
    }

    class Loading implements ProfileUiState {
        private final User user;
        private final String oldPassword;
        private final String newPassword;
        private final String confirmPassword;

        public Loading(User user, String oldPassword, String newPassword, String confirmPassword) {
            this.user = user;
            this.oldPassword = oldPassword;
            this.newPassword = newPassword;
            this.confirmPassword = confirmPassword;
        }

        @Override
        public User getUser() {
            return user;
        }

        @Override
        public String getOldPassword() {
            return oldPassword;
        }

        @Override
        public String getNewPassword() {
            return newPassword;
        }

        @Override
        public String getConfirmPassword() {
            return confirmPassword;
        }
    }

    class Error implements ProfileUiState {
        private final User user;
        private final String oldPassword;
        private final String newPassword;
        private final String confirmPassword;
        private final String message;

        public Error(User user, String oldPassword, String newPassword, String confirmPassword, String message) {
            this.user = user;
            this.oldPassword = oldPassword;
            this.newPassword = newPassword;
            this.confirmPassword = confirmPassword;
            this.message = message;
        }

        @Override
        public User getUser() {
            return user;
        }

        @Override
        public String getOldPassword() {
            return oldPassword;
        }

        @Override
        public String getNewPassword() {
            return newPassword;
        }

        @Override
        public String getConfirmPassword() {
            return confirmPassword;
        }

        public String getMessage() {
            return message;
        }
    }
}
