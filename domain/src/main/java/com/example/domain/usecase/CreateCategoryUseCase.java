package com.example.domain.usecase;

import com.example.domain.core.Result;
import com.example.domain.repository.TaskRepository;

import java.util.concurrent.CompletableFuture;

public class CreateCategoryUseCase {
    private final TaskRepository taskRepository;

    public CreateCategoryUseCase(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public CompletableFuture<Result<Void>> execute(String name, String colorHex) {
        return taskRepository.createCategory(name, colorHex);
    }
}
