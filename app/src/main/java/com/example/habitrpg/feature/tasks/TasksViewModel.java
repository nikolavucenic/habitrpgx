package com.example.habitrpg.feature.tasks;

import android.os.Handler;
import android.os.Looper;

import com.example.domain.core.Result;
import com.example.domain.model.TaskCategory;
import com.example.domain.model.TaskItem;
import com.example.domain.usecase.ChangeTaskStatusUseCase;
import com.example.domain.usecase.CreateCategoryUseCase;
import com.example.domain.usecase.CreateTaskUseCase;
import com.example.domain.usecase.GetCategoriesUseCase;
import com.example.domain.usecase.GetTasksUseCase;
import com.example.habitrpg.core.CoreViewModel;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class TasksViewModel extends CoreViewModel<TasksUiState, TasksAction, TasksSideEffect> {

    private final GetTasksUseCase getTasksUseCase;
    private final GetCategoriesUseCase getCategoriesUseCase;
    private final CreateTaskUseCase createTaskUseCase;
    private final CreateCategoryUseCase createCategoryUseCase;
    private final ChangeTaskStatusUseCase changeTaskStatusUseCase;

    @Inject
    public TasksViewModel(GetTasksUseCase getTasksUseCase,
                          GetCategoriesUseCase getCategoriesUseCase,
                          CreateTaskUseCase createTaskUseCase,
                          CreateCategoryUseCase createCategoryUseCase,
                          ChangeTaskStatusUseCase changeTaskStatusUseCase) {
        this.getTasksUseCase = getTasksUseCase;
        this.getCategoriesUseCase = getCategoriesUseCase;
        this.createTaskUseCase = createTaskUseCase;
        this.createCategoryUseCase = createCategoryUseCase;
        this.changeTaskStatusUseCase = changeTaskStatusUseCase;
        state.setValue(TasksUiState.initial());
    }

    @Override
    public void handleAction(TasksAction action) {
        if (action instanceof TasksAction.Load) {
            loadAll();
        } else if (action instanceof TasksAction.CreateCategory) {
            createCategory((TasksAction.CreateCategory) action);
        } else if (action instanceof TasksAction.CreateTask) {
            createTask((TasksAction.CreateTask) action);
        } else if (action instanceof TasksAction.ChangeStatus) {
            changeStatus((TasksAction.ChangeStatus) action);
        }
    }

    private void loadAll() {
        TasksUiState current = state.getValue();
        if (current == null) return;
        state.setValue(new TasksUiState.Loading(current.getCategories(), current.getTasks()));

        getCategoriesUseCase.execute().thenAccept(categoriesResult -> {
            if (categoriesResult instanceof Result.Error) {
                emitError(((Result.Error<List<TaskCategory>>) categoriesResult).message);
                return;
            }
            List<TaskCategory> categories = ((Result.Success<List<TaskCategory>>) categoriesResult).data;
            getTasksUseCase.execute().thenAccept(tasksResult -> {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (tasksResult instanceof Result.Success) {
                        state.setValue(new TasksUiState.Data(categories, ((Result.Success<List<TaskItem>>) tasksResult).data));
                    } else {
                        state.setValue(new TasksUiState.Error(categories, current.getTasks(), ((Result.Error<List<TaskItem>>) tasksResult).message));
                    }
                });
            });
        });
    }

    private void createCategory(TasksAction.CreateCategory action) {
        if (action.name.trim().isEmpty()) {
            sideEffect.setValue(new TasksSideEffect.ShowToast("Naziv kategorije je obavezan."));
            return;
        }
        createCategoryUseCase.execute(action.name.trim(), action.colorHex.trim())
                .thenAccept(result -> new Handler(Looper.getMainLooper()).post(() -> {
                    if (result instanceof Result.Success) {
                        sideEffect.setValue(new TasksSideEffect.ShowToast("Kategorija je sačuvana."));
                        loadAll();
                    } else {
                        sideEffect.setValue(new TasksSideEffect.ShowToast(((Result.Error<Void>) result).message));
                    }
                }));
    }

    private void createTask(TasksAction.CreateTask action) {
        createTaskUseCase.execute(action.task)
                .thenAccept(result -> new Handler(Looper.getMainLooper()).post(() -> {
                    if (result instanceof Result.Success) {
                        sideEffect.setValue(new TasksSideEffect.ShowToast("Zadatak je kreiran."));
                        loadAll();
                    } else {
                        sideEffect.setValue(new TasksSideEffect.ShowToast(((Result.Error<Void>) result).message));
                    }
                }));
    }

    private void changeStatus(TasksAction.ChangeStatus action) {
        changeTaskStatusUseCase.execute(action.taskId, action.newStatus)
                .thenAccept(result -> new Handler(Looper.getMainLooper()).post(() -> {
                    if (result instanceof Result.Success) {
                        sideEffect.setValue(new TasksSideEffect.ShowToast("Status je ažuriran."));
                        loadAll();
                    } else {
                        sideEffect.setValue(new TasksSideEffect.ShowToast(((Result.Error<Void>) result).message));
                    }
                }));
    }

    private void emitError(String message) {
        new Handler(Looper.getMainLooper()).post(() -> {
            TasksUiState current = state.getValue();
            if (current == null) return;
            state.setValue(new TasksUiState.Error(current.getCategories(), current.getTasks(), message));
        });
    }
}
