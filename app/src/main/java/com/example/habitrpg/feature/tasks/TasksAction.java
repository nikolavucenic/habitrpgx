package com.example.habitrpg.feature.tasks;

import com.example.domain.model.TaskItem;

public abstract class TasksAction {
    public static class Load extends TasksAction {}

    public static class OnFilterChanged extends TasksAction {
        public final int filter;

        public OnFilterChanged(int filter) {
            this.filter = filter;
        }
    }

    public static class SelectTask extends TasksAction {
        public final String taskId;

        public SelectTask(String taskId) {
            this.taskId = taskId;
        }
    }

    public static class CreateCategory extends TasksAction {
        public final String name;
        public final String colorHex;

        public CreateCategory(String name, String colorHex) {
            this.name = name;
            this.colorHex = colorHex;
        }
    }

    public static class UpdateCategory extends TasksAction {
        public final String categoryId;
        public final String name;
        public final String colorHex;

        public UpdateCategory(String categoryId, String name, String colorHex) {
            this.categoryId = categoryId;
            this.name = name;
            this.colorHex = colorHex;
        }
    }

    public static class DeleteCategory extends TasksAction {
        public final String categoryId;

        public DeleteCategory(String categoryId) {
            this.categoryId = categoryId;
        }
    }

    public static class CreateTask extends TasksAction {
        public final TaskItem task;

        public CreateTask(TaskItem task) {
            this.task = task;
        }
    }

    public static class UpdateTask extends TasksAction {
        public final TaskItem task;

        public UpdateTask(TaskItem task) {
            this.task = task;
        }
    }

    public static class DeleteTask extends TasksAction {
        public final String taskId;

        public DeleteTask(String taskId) {
            this.taskId = taskId;
        }
    }

    public static class ChangeStatus extends TasksAction {
        public final String taskId;
        public final String newStatus;

        public ChangeStatus(String taskId, String newStatus) {
            this.taskId = taskId;
            this.newStatus = newStatus;
        }
    }
}
