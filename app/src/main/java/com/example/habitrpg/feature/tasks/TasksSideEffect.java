package com.example.habitrpg.feature.tasks;

public abstract class TasksSideEffect {
    public static class ShowToast extends TasksSideEffect {
        public final String message;
        public ShowToast(String message) { this.message = message; }
    }
}
