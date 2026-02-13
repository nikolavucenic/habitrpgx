package com.example.habitrpg.feature.profile;

public abstract class ProfileSideEffect {
    public static class ShowToast extends ProfileSideEffect {
        public final String message;

        public ShowToast(String message) {
            this.message = message;
        }
    }

    public static class NavigateToLogin extends ProfileSideEffect {
    }
}
