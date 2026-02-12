package com.example.habitrpg.feature;

public abstract class SignUpUiState {
    private final String username;
    private final String email;
    private final String password;
    private final String confirmPassword;
    private final int avatarId;

    public SignUpUiState(String username, String email, String password, String confirmPassword, int avatarId) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.avatarId = avatarId;
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getConfirmPassword() { return confirmPassword; }
    public int getAvatarId() { return avatarId; }

    public static class Input extends SignUpUiState {
        public final String error;
        public Input(String username, String email, String password, String confirmPassword, int avatarId, String error) {
            super(username, email, password, confirmPassword, avatarId);
            this.error = error;
        }
    }

    public static class Loading extends SignUpUiState {
        public Loading(String username, String email, String password, String confirmPassword, int avatarId) {
            super(username, email, password, confirmPassword, avatarId);
        }
    }
}
