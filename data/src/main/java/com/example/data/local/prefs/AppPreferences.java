package com.example.data.local.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import javax.inject.Singleton;

@Singleton
public class AppPreferences {
    private static final String PREFS_NAME = "app_preferences";
    private static final String KEY_DARK_THEME_ENABLED = "dark_theme_enabled";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String KEY_SHAKE_ENABLED = "shake_enabled";

    private final SharedPreferences sharedPreferences;

    @Inject
    public AppPreferences(@ApplicationContext Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isDarkThemeEnabled() {
        return sharedPreferences.getBoolean(KEY_DARK_THEME_ENABLED, false);
    }

    public void setDarkThemeEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_DARK_THEME_ENABLED, enabled).apply();
    }

    public boolean areNotificationsEnabled() {
        return sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }

    public void setNotificationsEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply();
    }

    public boolean isShakeEnabled() {
        return sharedPreferences.getBoolean(KEY_SHAKE_ENABLED, true);
    }

    public void setShakeEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_SHAKE_ENABLED, enabled).apply();
    }
}
