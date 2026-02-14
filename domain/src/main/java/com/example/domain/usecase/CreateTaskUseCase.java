package com.example.domain.usecase;

import com.example.domain.core.Result;
import com.example.domain.model.TaskItem;
import com.example.domain.repository.TaskRepository;

import java.util.concurrent.CompletableFuture;

public class CreateTaskUseCase {
    private final TaskRepository taskRepository;

    public CreateTaskUseCase(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public CompletableFuture<Result<Void>> execute(TaskItem taskItem) {
        return taskRepository.createTask(taskItem);
    }
}
