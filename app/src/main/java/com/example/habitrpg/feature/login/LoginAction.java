package com.example.habitrpg.feature.login;

public abstract class LoginAction {
    public static class OnEmailChanged extends LoginAction {
        public final String email;
        public OnEmailChanged(String email) { this.email = email; }
    }

    public static class OnPasswordChanged extends LoginAction {
        public final String password;
        public OnPasswordChanged(String password) { this.password = password; }
    }

    public static class OnLoginClicked extends LoginAction {}
    public static class OnGoToRegisterClicked extends LoginAction {}
    public static class OnGoToForgotPasswordClicked extends LoginAction {}
}
