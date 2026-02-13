package com.example.habitrpg.feature.dashboard;

public class DashboardUiState {
    public final boolean loading;
    public final String username;
    public final String title;
    public final int level;
    public final int pp;
    public final int currentXp;
    public final int requiredXp;
    public final String importancePreview;
    public final String difficultyPreview;
    public final String error;

    public DashboardUiState(boolean loading,
                            String username,
                            String title,
                            int level,
                            int pp,
                            int currentXp,
                            int requiredXp,
                            String importancePreview,
                            String difficultyPreview,
                            String error) {
        this.loading = loading;
        this.username = username;
        this.title = title;
        this.level = level;
        this.pp = pp;
        this.currentXp = currentXp;
        this.requiredXp = requiredXp;
        this.importancePreview = importancePreview;
        this.difficultyPreview = difficultyPreview;
        this.error = error;
    }

    public static DashboardUiState loading() {
        return new DashboardUiState(true, "", "", 1, 0, 0, 200, "", "", null);
    }
}
