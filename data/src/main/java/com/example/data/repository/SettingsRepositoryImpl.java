package com.example.data.repository;

import com.example.data.local.prefs.AppPreferences;
import com.example.domain.repository.SettingsRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SettingsRepositoryImpl implements SettingsRepository {
    private final AppPreferences appPreferences;

    @Inject
    public SettingsRepositoryImpl(AppPreferences appPreferences) {
        this.appPreferences = appPreferences;
    }

    @Override
    public boolean isDarkThemeEnabled() {
        return appPreferences.isDarkThemeEnabled();
    }

    @Override
    public void setDarkThemeEnabled(boolean enabled) {
        appPreferences.setDarkThemeEnabled(enabled);
    }

    @Override
    public boolean areNotificationsEnabled() {
        return appPreferences.areNotificationsEnabled();
    }

    @Override
    public void setNotificationsEnabled(boolean enabled) {
        appPreferences.setNotificationsEnabled(enabled);
    }

    @Override
    public boolean isShakeEnabled() {
        return appPreferences.isShakeEnabled();
    }

    @Override
    public void setShakeEnabled(boolean enabled) {
        appPreferences.setShakeEnabled(enabled);
    }
}
