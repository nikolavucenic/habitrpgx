package com.example.domain.usecase;

import com.example.domain.core.Result;
import com.example.domain.model.TaskItem;
import com.example.domain.repository.TaskRepository;

import java.util.concurrent.CompletableFuture;

public class UpdateTaskUseCase {
    private final TaskRepository taskRepository;

    public UpdateTaskUseCase(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public CompletableFuture<Result<Void>> execute(TaskItem taskItem) {
        TaskItem normalized = new TaskItem(
                taskItem.getId(),
                taskItem.getTitle(),
                taskItem.getDescription(),
                taskItem.getCategoryId(),
                taskItem.getCategoryName(),
                taskItem.getCategoryColorHex(),
                taskItem.getType(),
                taskItem.getRepeatInterval(),
                taskItem.getRepeatUnit(),
                taskItem.getRepeatStartAt(),
                taskItem.getRepeatEndAt(),
                taskItem.getExecuteAt(),
                taskItem.getDifficulty(),
                taskItem.getImportance(),
                TaskRules.xpValue(taskItem.getDifficulty(), taskItem.getImportance()),
                taskItem.getStatus(),
                taskItem.getCreatedAt()
        );
        return taskRepository.updateTask(normalized);
    }
}
