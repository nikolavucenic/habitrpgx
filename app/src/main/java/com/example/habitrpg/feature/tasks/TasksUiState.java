package com.example.habitrpg.feature.tasks;

import com.example.domain.model.TaskCategory;
import com.example.domain.model.TaskItem;

import java.util.Collections;
import java.util.List;

public abstract class TasksUiState {
    public static final int FILTER_ALL = 0;
    public static final int FILTER_ONE_TIME = 1;
    public static final int FILTER_REPEATING = 2;

    private final List<TaskCategory> categories;
    private final List<TaskItem> tasks;
    private final List<TaskItem> filteredTasks;
    private final int selectedFilter;
    private final TaskItem selectedTask;

    protected TasksUiState(List<TaskCategory> categories,
                           List<TaskItem> tasks,
                           List<TaskItem> filteredTasks,
                           int selectedFilter,
                           TaskItem selectedTask) {
        this.categories = categories;
        this.tasks = tasks;
        this.filteredTasks = filteredTasks;
        this.selectedFilter = selectedFilter;
        this.selectedTask = selectedTask;
    }

    public List<TaskCategory> getCategories() { return categories; }
    public List<TaskItem> getTasks() { return tasks; }
    public List<TaskItem> getFilteredTasks() { return filteredTasks; }
    public int getSelectedFilter() { return selectedFilter; }
    public TaskItem getSelectedTask() { return selectedTask; }

    public static class Loading extends TasksUiState {
        public Loading(List<TaskCategory> categories, List<TaskItem> tasks, List<TaskItem> filteredTasks, int selectedFilter, TaskItem selectedTask) {
            super(categories, tasks, filteredTasks, selectedFilter, selectedTask);
        }
    }

    public static class Data extends TasksUiState {
        public Data(List<TaskCategory> categories, List<TaskItem> tasks, List<TaskItem> filteredTasks, int selectedFilter, TaskItem selectedTask) {
            super(categories, tasks, filteredTasks, selectedFilter, selectedTask);
        }
    }

    public static class Error extends TasksUiState {
        private final String message;

        public Error(List<TaskCategory> categories,
                     List<TaskItem> tasks,
                     List<TaskItem> filteredTasks,
                     int selectedFilter,
                     TaskItem selectedTask,
                     String message) {
            super(categories, tasks, filteredTasks, selectedFilter, selectedTask);
            this.message = message;
        }

        public String getMessage() { return message; }
    }

    public static TasksUiState initial() {
        return new Loading(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), FILTER_ALL, null);
    }
}
