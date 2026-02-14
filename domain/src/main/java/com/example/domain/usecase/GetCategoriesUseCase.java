package com.example.domain.usecase;

import com.example.domain.core.Result;
import com.example.domain.model.TaskCategory;
import com.example.domain.repository.TaskRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GetCategoriesUseCase {
    private final TaskRepository taskRepository;

    public GetCategoriesUseCase(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public CompletableFuture<Result<List<TaskCategory>>> execute() {
        return taskRepository.getCategories();
    }
}
