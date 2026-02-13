package com.example.habitrpg.feature.login;

import com.example.domain.model.User;

public interface LoginUiState {
    String getEmail();
    String getPassword();

    class Input implements LoginUiState {
        private final String email;
        private final String password;
        private final String emailError;
        private final String passwordError;

        public Input(String email, String password, String emailError, String passwordError) {
            this.email = email;
            this.password = password;
            this.emailError = emailError;
            this.passwordError = passwordError;
        }

        @Override public String getEmail() { return email; }
        @Override public String getPassword() { return password; }
        public String getEmailError() { return emailError; }
        public String getPasswordError() { return passwordError; }
    }

    class Loading implements LoginUiState {
        private final String email;
        private final String password;

        public Loading(String email, String password) {
            this.email = email;
            this.password = password;
        }

        @Override public String getEmail() { return email; }
        @Override public String getPassword() { return password; }
    }

    class Error implements LoginUiState {
        private final String email;
        private final String password;
        private final String emailError;
        private final String passwordError;

        public Error(String email, String password, String emailError, String passwordError) {
            this.email = email;
            this.password = password;
            this.emailError = emailError;
            this.passwordError = passwordError;
        }

        @Override public String getEmail() { return email; }
        @Override public String getPassword() { return password; }
        public String getEmailError() { return emailError; }
        public String getPasswordError() { return passwordError; }
    }
}