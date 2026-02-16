package com.example.domain.usecase;

import com.example.domain.core.Result;
import com.example.domain.repository.TaskRepository;

import java.util.concurrent.CompletableFuture;

public class DeleteCategoryUseCase {
    private final TaskRepository taskRepository;

    public DeleteCategoryUseCase(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public CompletableFuture<Result<Void>> execute(String categoryId) {
        return taskRepository.deleteCategory(categoryId);
    }
}
