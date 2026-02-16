package com.example.domain.usecase;

import com.example.domain.core.Result;
import com.example.domain.model.TaskItem;
import com.example.domain.repository.TaskRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ChangeTaskStatusUseCase {
    private final TaskRepository taskRepository;

    public ChangeTaskStatusUseCase(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public CompletableFuture<Result<Void>> execute(String taskId, String newStatus) {
        long now = System.currentTimeMillis();
        return taskRepository.getTaskById(taskId).thenCompose(taskResult -> {
            if (taskResult instanceof Result.Error) {
                return CompletableFuture.completedFuture(new Result.Error<>(((Result.Error<TaskItem>) taskResult).message));
            }
            TaskItem task = ((Result.Success<TaskItem>) taskResult).data;
            String validation = TaskRules.validateStatusTransition(task, newStatus, now);
            if (validation != null) {
                return CompletableFuture.completedFuture(new Result.Error<>(validation));
            }

            if (TaskItem.STATUS_DONE.equals(newStatus) && TaskRules.shouldAutoMarkNotDone(task, now)) {
                return taskRepository.changeTaskStatus(taskId, TaskItem.STATUS_NOT_DONE)
                        .thenApply(ignored -> new Result.Error<Void>("Prošao je rok od 3 dana. Zadatak je označen kao neurađen."));
            }

            if (!TaskItem.STATUS_DONE.equals(newStatus)) {
                return taskRepository.changeTaskStatus(taskId, newStatus);
            }

            return taskRepository.getTasks().thenCompose(tasksResult -> {
                if (tasksResult instanceof Result.Error) {
                    return CompletableFuture.completedFuture(new Result.Error<>(((Result.Error<List<TaskItem>>) tasksResult).message));
                }
                List<TaskItem> tasks = ((Result.Success<List<TaskItem>>) tasksResult).data;
                int doneCount = TaskRules.doneCountInQuotaWindow(task, tasks, now);
                int quota = TaskRules.quotaLimit(task);
                int awardedXp = doneCount >= quota ? 0 : task.getXpValue();

                return taskRepository.changeTaskStatus(taskId, TaskItem.STATUS_DONE)
                        .thenCompose(statusResult -> {
                            if (statusResult instanceof Result.Error) {
                                return CompletableFuture.completedFuture(statusResult);
                            }
                            if (awardedXp <= 0) {
                                return CompletableFuture.completedFuture(new Result.Success<>(null));
                            }
                            return taskRepository.incrementCurrentUserXp(awardedXp);
                        });
            });
        });
    }
}
