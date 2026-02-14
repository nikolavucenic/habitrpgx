package com.example.habitrpg.feature.tasks;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.domain.model.TaskCategory;
import com.example.domain.model.TaskItem;
import com.example.habitrpg.core.CoreFragment;
import com.example.habitrpg.databinding.FragmentTasksBinding;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TasksFragment extends CoreFragment<FragmentTasksBinding> {

    private TasksViewModel viewModel;
    private CategoryAdapter categoryAdapter;
    private TaskAdapter taskAdapter;
    private List<TaskCategory> currentCategories = new ArrayList<>();

    @Override
    protected FragmentTasksBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentTasksBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TasksViewModel.class);
        setupLists();
        setupListeners();
        setupObservers();
        viewModel.handleAction(new TasksAction.Load());
    }

    private void setupLists() {
        categoryAdapter = new CategoryAdapter();
        taskAdapter = new TaskAdapter(new TaskAdapter.TaskStatusListener() {
            @Override public void onSetDone(String taskId) { viewModel.handleAction(new TasksAction.ChangeStatus(taskId, TaskItem.STATUS_DONE)); }
            @Override public void onSetCanceled(String taskId) { viewModel.handleAction(new TasksAction.ChangeStatus(taskId, TaskItem.STATUS_CANCELED)); }
            @Override public void onSetPaused(String taskId) { viewModel.handleAction(new TasksAction.ChangeStatus(taskId, TaskItem.STATUS_PAUSED)); }
            @Override public void onSetActive(String taskId) { viewModel.handleAction(new TasksAction.ChangeStatus(taskId, TaskItem.STATUS_ACTIVE)); }
        });

        getBinding().rvCategories.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        getBinding().rvCategories.setAdapter(categoryAdapter);

        getBinding().rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        getBinding().rvTasks.setAdapter(taskAdapter);
    }

    private void setupListeners() {
        getBinding().btnAddCategory.setOnClickListener(v -> showCreateCategoryDialog());
        getBinding().btnAddTask.setOnClickListener(v -> showCreateTaskDialog());
        getBinding().btnRefresh.setOnClickListener(v -> viewModel.handleAction(new TasksAction.Load()));
    }

    private void setupObservers() {
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            getBinding().progressBar.setVisibility(state instanceof TasksUiState.Loading ? View.VISIBLE : View.GONE);
            if (state instanceof TasksUiState.Error) {
                getBinding().tvError.setVisibility(View.VISIBLE);
                getBinding().tvError.setText(((TasksUiState.Error) state).getMessage());
            } else {
                getBinding().tvError.setVisibility(View.GONE);
            }

            currentCategories = state.getCategories();
            categoryAdapter.submit(state.getCategories());
            taskAdapter.submit(state.getTasks());
        });

        viewModel.getEffect().observe(getViewLifecycleOwner(), effect -> {
            if (effect instanceof TasksSideEffect.ShowToast) {
                Toast.makeText(requireContext(), ((TasksSideEffect.ShowToast) effect).message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCreateCategoryDialog() {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);

        EditText etName = new EditText(requireContext());
        etName.setHint("Naziv kategorije");
        layout.addView(etName);

        EditText etColor = new EditText(requireContext());
        etColor.setHint("Boja (npr. #4CAF50)");
        layout.addView(etColor);

        new AlertDialog.Builder(requireContext())
                .setTitle("Nova kategorija")
                .setView(layout)
                .setPositiveButton("Sačuvaj", (dialog, which) ->
                        viewModel.handleAction(new TasksAction.CreateCategory(etName.getText().toString(), etColor.getText().toString())))
                .setNegativeButton("Otkaži", null)
                .show();
    }

    private void showCreateTaskDialog() {
        if (currentCategories.isEmpty()) {
            Toast.makeText(requireContext(), "Prvo kreirajte kategoriju.", Toast.LENGTH_SHORT).show();
            return;
        }

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);

        EditText etTitle = new EditText(requireContext());
        etTitle.setHint("Naziv zadatka");
        layout.addView(etTitle);

        EditText etDescription = new EditText(requireContext());
        etDescription.setHint("Opis (opciono)");
        layout.addView(etDescription);

        Spinner categorySpinner = new Spinner(requireContext());
        List<String> categoryNames = new ArrayList<>();
        for (TaskCategory category : currentCategories) categoryNames.add(category.getName());
        categorySpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categoryNames));
        layout.addView(categorySpinner);

        Spinner difficultySpinner = new Spinner(requireContext());
        String[] difficulties = {"VEOMA_LAK", "LAK", "TEZAK", "EKSTREMNO_TEZAK"};
        difficultySpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, difficulties));
        layout.addView(difficultySpinner);

        Spinner importanceSpinner = new Spinner(requireContext());
        String[] importance = {"NORMALAN", "VAZAN", "EKSTREMNO_VAZAN", "SPECIJALAN"};
        importanceSpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, importance));
        layout.addView(importanceSpinner);

        Spinner typeSpinner = new Spinner(requireContext());
        String[] types = {TaskItem.TYPE_ONE_TIME, TaskItem.TYPE_REPEATING};
        typeSpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, types));
        layout.addView(typeSpinner);

        new AlertDialog.Builder(requireContext())
                .setTitle("Novi zadatak")
                .setView(layout)
                .setPositiveButton("Sačuvaj", (dialog, which) -> {
                    int selectedCategory = categorySpinner.getSelectedItemPosition();
                    TaskCategory category = currentCategories.get(selectedCategory);
                    String difficulty = difficulties[difficultySpinner.getSelectedItemPosition()];
                    String importanceValue = importance[importanceSpinner.getSelectedItemPosition()];
                    int xp = difficultyXp(difficulty) + importanceXp(importanceValue);

                    TaskItem task = new TaskItem(
                            "",
                            etTitle.getText().toString(),
                            etDescription.getText().toString(),
                            category.getId(),
                            category.getName(),
                            category.getColorHex(),
                            types[typeSpinner.getSelectedItemPosition()],
                            1,
                            "DAY",
                            System.currentTimeMillis(),
                            0,
                            System.currentTimeMillis(),
                            difficulty,
                            importanceValue,
                            xp,
                            TaskItem.STATUS_ACTIVE,
                            System.currentTimeMillis()
                    );
                    viewModel.handleAction(new TasksAction.CreateTask(task));
                })
                .setNegativeButton("Otkaži", null)
                .show();
    }

    private int difficultyXp(String value) {
        switch (value) {
            case "VEOMA_LAK": return 1;
            case "LAK": return 3;
            case "TEZAK": return 7;
            case "EKSTREMNO_TEZAK": return 20;
            default: return 0;
        }
    }

    private int importanceXp(String value) {
        switch (value) {
            case "NORMALAN": return 1;
            case "VAZAN": return 3;
            case "EKSTREMNO_VAZAN": return 10;
            case "SPECIJALAN": return 100;
            default: return 0;
        }
    }
}
