package com.example.habitrpg.feature.progression;

public class ProgressionUiState {
    public final String username;
    public final String title;
    public final int level;
    public final int pp;
    public final int currentXp;
    public final int requiredXp;
    public final String importancePreview;
    public final String difficultyPreview;
    public final String error;

    public ProgressionUiState(String username,
                              String title,
                              int level,
                              int pp,
                              int currentXp,
                              int requiredXp,
                              String importancePreview,
                              String difficultyPreview,
                              String error) {
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

    public static ProgressionUiState initial() {
        return new ProgressionUiState("Heroj", "Početnik navika", 1, 0, 0, 200, "", "", null);
    }
}
