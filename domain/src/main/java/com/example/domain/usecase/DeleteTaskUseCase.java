package com.example.domain.usecase;

import com.example.domain.core.Result;
import com.example.domain.repository.TaskRepository;

import java.util.concurrent.CompletableFuture;

public class DeleteTaskUseCase {
    private final TaskRepository taskRepository;

    public DeleteTaskUseCase(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public CompletableFuture<Result<Void>> execute(String taskId) {
        return taskRepository.deleteTask(taskId);
    }
}
