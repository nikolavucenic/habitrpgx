package com.example.domain.usecase;

import com.example.domain.core.Result;
import com.example.domain.model.TaskItem;
import com.example.domain.repository.TaskRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CreateTaskUseCase {
    private final TaskRepository taskRepository;

    public CreateTaskUseCase(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public CompletableFuture<Result<Void>> execute(TaskItem taskItem) {
        int xp = TaskRules.xpValue(taskItem.getDifficulty(), taskItem.getImportance());
        List<TaskItem> instances = buildInstances(taskItem, xp);
        CompletableFuture<Result<Void>> chain = CompletableFuture.completedFuture(new Result.Success<>(null));
        for (TaskItem instance : instances) {
            chain = chain.thenCompose(prev -> {
                if (prev instanceof Result.Error) return CompletableFuture.completedFuture(prev);
                return taskRepository.createTask(instance);
            });
        }
        return chain;
    }

    private List<TaskItem> buildInstances(TaskItem source, int xpValue) {
        List<TaskItem> result = new ArrayList<>();
        if (!TaskItem.TYPE_REPEATING.equals(source.getType())) {
            result.add(copyWithExecuteAt(source, source.getExecuteAt(), xpValue));
            return result;
        }

        long start = source.getRepeatStartAt() == 0L ? source.getExecuteAt() : source.getRepeatStartAt();
        long end = source.getRepeatEndAt();
        if (end == 0L || end < start) {
            result.add(copyWithExecuteAt(source, source.getExecuteAt(), xpValue));
            return result;
        }

        long step = "WEEK".equals(source.getRepeatUnit())
                ? source.getRepeatInterval() * 7L * 24 * 60 * 60 * 1000
                : source.getRepeatInterval() * 24L * 60 * 60 * 1000;
        if (step <= 0) step = 24L * 60 * 60 * 1000;

        for (long at = start; at <= end; at += step) {
            result.add(copyWithExecuteAt(source, at, xpValue));
        }

        return result;
    }

    private TaskItem copyWithExecuteAt(TaskItem src, long executeAt, int xpValue) {
        return new TaskItem(
                src.getId(),
                src.getTitle(),
                src.getDescription(),
                src.getCategoryId(),
                src.getCategoryName(),
                src.getCategoryColorHex(),
                src.getType(),
                src.getRepeatInterval(),
                src.getRepeatUnit(),
                src.getRepeatStartAt(),
                src.getRepeatEndAt(),
                executeAt,
                src.getDifficulty(),
                src.getImportance(),
                xpValue,
                src.getStatus(),
                src.getCreatedAt()
        );
    }
}
