package com.example.domain.model;

public class ActivationToken {
    private final String token;
    private final String userId;
    private final long createdAtEpochMillis;

    public ActivationToken(String token, String userId, long createdAtEpochMillis) {
        this.token = token;
        this.userId = userId;
        this.createdAtEpochMillis = createdAtEpochMillis;
    }

    public String getToken() {
        return token;
    }

    public String getUserId() {
        return userId;
    }

    public long getCreatedAtEpochMillis() {
        return createdAtEpochMillis;
    }
}
