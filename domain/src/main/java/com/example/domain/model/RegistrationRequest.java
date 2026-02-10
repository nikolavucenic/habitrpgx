package com.example.domain.model;

public class RegistrationRequest {
    private final String email;
    private final String password;
    private final String confirmPassword;
    private final String username;
    private final int avatarId;

    public RegistrationRequest(String email, String password, String confirmPassword, String username, int avatarId) {
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.username = username;
        this.avatarId = avatarId;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public String getUsername() {
        return username;
    }

    public int getAvatarId() {
        return avatarId;
    }
}
