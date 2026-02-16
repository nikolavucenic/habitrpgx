package com.example.domain.usecase;

import com.example.domain.core.Result;
import com.example.domain.repository.TaskRepository;

import java.util.concurrent.CompletableFuture;

public class UpdateCategoryUseCase {
    private final TaskRepository taskRepository;

    public UpdateCategoryUseCase(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public CompletableFuture<Result<Void>> execute(String categoryId, String name, String colorHex) {
        return taskRepository.updateCategory(categoryId, name, colorHex);
    }
}
