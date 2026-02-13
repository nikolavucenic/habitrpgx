package com.example.habitrpg.feature.signup;

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
        public final String usernameError;
        public final String emailError;
        public final String passwordError;
        public final String confirmPasswordError;

        public Input(
                String username,
                String email,
                String password,
                String confirmPassword,
                int avatarId,
                String usernameError,
                String emailError,
                String passwordError,
                String confirmPasswordError
        ) {
            super(username, email, password, confirmPassword, avatarId);
            this.usernameError = usernameError;
            this.emailError = emailError;
            this.passwordError = passwordError;
            this.confirmPasswordError = confirmPasswordError;
        }
    }

    public static class Loading extends SignUpUiState {
        public Loading(String username, String email, String password, String confirmPassword, int avatarId) {
            super(username, email, password, confirmPassword, avatarId);
        }
    }
}
