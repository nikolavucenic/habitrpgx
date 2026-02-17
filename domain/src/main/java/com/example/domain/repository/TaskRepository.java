package com.example.domain.repository;

import com.example.domain.core.Result;
import com.example.domain.model.TaskCategory;
import com.example.domain.model.TaskItem;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TaskRepository {
    boolean isPendingBossEncounter();
    void setPendingBossEncounter(boolean pending);
    int getLastResolvedBossEncounterLevel();
    void saveLastResolvedBossEncounterLevel(int encounterLevel);
    int getBossNumber();
    int getBossHp();
    void saveBossState(int bossNumber, int hp);

    CompletableFuture<Result<List<TaskCategory>>> getCategories();
    CompletableFuture<Result<Void>> createCategory(String name, String colorHex);
    CompletableFuture<Result<Void>> updateCategory(String categoryId, String name, String colorHex);
    CompletableFuture<Result<Void>> deleteCategory(String categoryId);

    CompletableFuture<Result<List<TaskItem>>> getTasks();
    CompletableFuture<Result<TaskItem>> getTaskById(String taskId);
    CompletableFuture<Result<Void>> createTask(TaskItem taskItem);
    CompletableFuture<Result<Void>> updateTask(TaskItem taskItem);
    CompletableFuture<Result<Void>> deleteTask(String taskId);
    CompletableFuture<Result<Void>> changeTaskStatus(String taskId, String newStatus);
    CompletableFuture<Result<Void>> incrementCurrentUserXp(int xp);
}
