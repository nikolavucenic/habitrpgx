package com.example.habitrpg.feature.signup;

public abstract class SignUpSideEffect {
    public static class ShowToast extends SignUpSideEffect {
        public final String message;
        public ShowToast(String message) { this.message = message; }
    }

    public static class NavigateToLogin extends SignUpSideEffect {}
}
