package com.example.habitrpg.feature;

public interface LoginUiState {
    String getEmail();
    String getPassword();

    // Input stanje
    class Input implements LoginUiState {
        private final String email;
        private final String password;
        private final String emailError;

        public Input(String email, String password, String emailError) {
            this.email = email;
            this.password = password;
            this.emailError = emailError;
        }

        @Override public String getEmail() { return email; }
        @Override public String getPassword() { return password; }
        public String getEmailError() { return emailError; }
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
        private final String errorMessage;

        public Error(String email, String password, String errorMessage) {
            this.email = email;
            this.password = password;
            this.errorMessage = errorMessage;
        }

        @Override public String getEmail() { return email; }
        @Override public String getPassword() { return password; }
        public String getErrorMessage() { return errorMessage; }
    }
}