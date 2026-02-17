package com.example.habitrpg.feature.forgotpassword;

public interface ForgotPasswordUiState {
    String getEmail();

    class Input implements ForgotPasswordUiState {
        private final String email;
        private final String emailError;

        public Input(String email, String emailError) {
            this.email = email;
            this.emailError = emailError;
        }

        @Override
        public String getEmail() {
            return email;
        }

        public String getEmailError() {
            return emailError;
        }
    }

    class Loading implements ForgotPasswordUiState {
        private final String email;

        public Loading(String email) {
            this.email = email;
        }

        @Override
        public String getEmail() {
            return email;
        }
    }
}
