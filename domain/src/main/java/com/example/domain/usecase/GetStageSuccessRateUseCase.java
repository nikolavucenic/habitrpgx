package com.example.domain.usecase;

import com.example.domain.core.Result;
import com.example.domain.model.TaskItem;
import com.example.domain.repository.TaskRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GetStageSuccessRateUseCase {

    private final TaskRepository taskRepository;

    public GetStageSuccessRateUseCase(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public CompletableFuture<Result<Integer>> execute() {
        return taskRepository.getTasks().thenApply(result -> {
            if (result instanceof Result.Error) {
                return new Result.Error<Integer>(((Result.Error<List<TaskItem>>) result).message);
            }

            List<TaskItem> tasks = ((Result.Success<List<TaskItem>>) result).data;
            int eligible = 0;
            int completed = 0;
            for (TaskItem item : tasks) {
                String status = item.getStatus();
                if (TaskItem.STATUS_PAUSED.equals(status) || TaskItem.STATUS_CANCELED.equals(status)) {
                    continue;
                }
                eligible++;
                if (TaskItem.STATUS_DONE.equals(status)) {
                    completed++;
                }
            }

            int successRate = eligible == 0 ? 0 : Math.round((completed * 100f) / eligible);
            return new Result.Success<>(Math.max(0, Math.min(100, successRate)));
        });
    }
}
