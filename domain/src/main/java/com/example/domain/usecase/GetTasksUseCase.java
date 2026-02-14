package com.example.domain.usecase;

import com.example.domain.core.Result;
import com.example.domain.model.TaskItem;
import com.example.domain.repository.TaskRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GetTasksUseCase {
    private final TaskRepository taskRepository;

    public GetTasksUseCase(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public CompletableFuture<Result<List<TaskItem>>> execute() {
        return taskRepository.getTasks();
    }
}
