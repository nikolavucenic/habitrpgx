package com.example.habitrpg.feature.signup;

public abstract class SignUpAction {
    public static class OnUsernameChanged extends SignUpAction { public final String username; public OnUsernameChanged(String username) { this.username = username; } }
    public static class OnEmailChanged extends SignUpAction { public final String email; public OnEmailChanged(String email) { this.email = email; } }
    public static class OnPasswordChanged extends SignUpAction { public final String password; public OnPasswordChanged(String password) { this.password = password; } }
    public static class OnConfirmPasswordChanged extends SignUpAction { public final String confirmPassword; public OnConfirmPasswordChanged(String confirmPassword) { this.confirmPassword = confirmPassword; } }
    public static class OnAvatarSelected extends SignUpAction { public final int avatarId; public OnAvatarSelected(int avatarId) { this.avatarId = avatarId; } }
    public static class OnRegisterClicked extends SignUpAction {}
    public static class OnBackToLoginClicked extends SignUpAction {}
}
