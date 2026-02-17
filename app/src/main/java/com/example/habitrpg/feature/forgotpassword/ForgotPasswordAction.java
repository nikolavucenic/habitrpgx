package com.example.habitrpg.feature.forgotpassword;

public abstract class ForgotPasswordAction {
    public static class OnEmailChanged extends ForgotPasswordAction {
        public final String email;

        public OnEmailChanged(String email) {
            this.email = email;
        }
    }

    public static class OnSendResetEmailClicked extends ForgotPasswordAction {}

    public static class OnBackToLoginClicked extends ForgotPasswordAction {}
}
