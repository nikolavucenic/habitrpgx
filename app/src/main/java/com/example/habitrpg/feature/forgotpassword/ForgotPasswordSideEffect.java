package com.example.habitrpg.feature.forgotpassword;

public abstract class ForgotPasswordSideEffect {
    public static class ShowToast extends ForgotPasswordSideEffect {
        public final String message;

        public ShowToast(String message) {
            this.message = message;
        }
    }

    public static class NavigateBackToLogin extends ForgotPasswordSideEffect {}
}
