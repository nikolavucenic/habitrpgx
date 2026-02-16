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

import java.util.ArrayList;
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
        } else if (action instanceof TasksAction.OnFilterChanged) {
            applyFilterAndEmit(((TasksAction.OnFilterChanged) action).filter);
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

        state.setValue(new TasksUiState.Loading(
                current.getCategories(),
                current.getTasks(),
                current.getFilteredTasks(),
                current.getSelectedFilter()
        ));

        getCategoriesUseCase.execute().thenAccept(categoriesResult -> {
            if (categoriesResult instanceof Result.Error) {
                emitError(((Result.Error<List<TaskCategory>>) categoriesResult).message);
                return;
            }

            List<TaskCategory> categories = ((Result.Success<List<TaskCategory>>) categoriesResult).data;
            getTasksUseCase.execute().thenAccept(tasksResult ->
                    new Handler(Looper.getMainLooper()).post(() -> {
                        TasksUiState latest = state.getValue();
                        int selectedFilter = latest == null ? TasksUiState.FILTER_ALL : latest.getSelectedFilter();

                        if (tasksResult instanceof Result.Success) {
                            List<TaskItem> tasks = ((Result.Success<List<TaskItem>>) tasksResult).data;
                            state.setValue(new TasksUiState.Data(
                                    categories,
                                    tasks,
                                    filterTasks(tasks, selectedFilter),
                                    selectedFilter
                            ));
                        } else {
                            List<TaskItem> fallbackTasks = latest == null ? new ArrayList<>() : latest.getTasks();
                            state.setValue(new TasksUiState.Error(
                                    categories,
                                    fallbackTasks,
                                    filterTasks(fallbackTasks, selectedFilter),
                                    selectedFilter,
                                    ((Result.Error<List<TaskItem>>) tasksResult).message
                            ));
                        }
                    }));
        });
    }

    private void applyFilterAndEmit(int filter) {
        TasksUiState current = state.getValue();
        if (current == null) return;

        List<TaskItem> filtered = filterTasks(current.getTasks(), filter);

        if (current instanceof TasksUiState.Loading) {
            state.setValue(new TasksUiState.Loading(current.getCategories(), current.getTasks(), filtered, filter));
        } else if (current instanceof TasksUiState.Error) {
            state.setValue(new TasksUiState.Error(
                    current.getCategories(),
                    current.getTasks(),
                    filtered,
                    filter,
                    ((TasksUiState.Error) current).getMessage()
            ));
        } else {
            state.setValue(new TasksUiState.Data(current.getCategories(), current.getTasks(), filtered, filter));
        }
    }

    private List<TaskItem> filterTasks(List<TaskItem> source, int filter) {
        List<TaskItem> filtered = new ArrayList<>();
        for (TaskItem task : source) {
            if (filter == TasksUiState.FILTER_ALL) {
                filtered.add(task);
            } else if (filter == TasksUiState.FILTER_ONE_TIME && TaskItem.TYPE_ONE_TIME.equals(task.getType())) {
                filtered.add(task);
            } else if (filter == TasksUiState.FILTER_REPEATING && TaskItem.TYPE_REPEATING.equals(task.getType())) {
                filtered.add(task);
            }
        }
        return filtered;
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
            state.setValue(new TasksUiState.Error(
                    current.getCategories(),
                    current.getTasks(),
                    current.getFilteredTasks(),
                    current.getSelectedFilter(),
                    message
            ));
        });
    }
}
