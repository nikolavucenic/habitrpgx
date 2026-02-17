package com.example.habitrpg.feature.social;

public abstract class SocialSideEffect {
    public static class ShowToast extends SocialSideEffect {
        public final String message;
        public ShowToast(String message) { this.message = message; }
    }
}
