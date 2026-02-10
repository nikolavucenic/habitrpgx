package com.example.domain.repository;

public interface SettingsRepository {
    boolean isDarkThemeEnabled();
    void setDarkThemeEnabled(boolean enabled);

    boolean areNotificationsEnabled();
    void setNotificationsEnabled(boolean enabled);

    boolean isShakeEnabled();
    void setShakeEnabled(boolean enabled);
}
