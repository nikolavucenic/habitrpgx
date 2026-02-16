package com.example.habitrpg.feature.tasks;

import com.example.domain.model.TaskCategory;
import com.example.domain.model.TaskItem;

import java.util.Collections;
import java.util.List;

public abstract class TasksUiState {
    private final List<TaskCategory> categories;
    private final List<TaskItem> tasks;

    protected TasksUiState(List<TaskCategory> categories, List<TaskItem> tasks) {
        this.categories = categories;
        this.tasks = tasks;
    }

    public List<TaskCategory> getCategories() { return categories; }
    public List<TaskItem> getTasks() { return tasks; }

    public static class Loading extends TasksUiState {
        public Loading(List<TaskCategory> categories, List<TaskItem> tasks) { super(categories, tasks); }
    }

    public static class Data extends TasksUiState {
        public Data(List<TaskCategory> categories, List<TaskItem> tasks) { super(categories, tasks); }
    }

    public static class Error extends TasksUiState {
        private final String message;
        public Error(List<TaskCategory> categories, List<TaskItem> tasks, String message) {
            super(categories, tasks);
            this.message = message;
        }
        public String getMessage() { return message; }
    }

    public static TasksUiState initial() {
        return new Loading(Collections.emptyList(), Collections.emptyList());
    }
}
