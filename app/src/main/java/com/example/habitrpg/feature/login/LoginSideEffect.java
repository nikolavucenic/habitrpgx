package com.example.habitrpg.feature.login;

public abstract class LoginSideEffect {
    public static class NavigateToHome extends LoginSideEffect {}
    public static class NavigateToRegister extends LoginSideEffect {}
    public static class ShowToast extends LoginSideEffect {
        public final String message;
        public ShowToast(String message) { this.message = message; }
    }
}