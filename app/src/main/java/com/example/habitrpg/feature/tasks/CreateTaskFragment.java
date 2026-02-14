package com.example.habitrpg.feature.tasks;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.domain.model.TaskCategory;
import com.example.domain.model.TaskItem;
import com.example.habitrpg.core.CoreFragment;
import com.example.habitrpg.databinding.FragmentCreateTaskBinding;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CreateTaskFragment extends CoreFragment<FragmentCreateTaskBinding> {

    private TasksViewModel viewModel;
    private List<TaskCategory> categories = new ArrayList<>();
    private final String[] difficulties = {"VEOMA_LAK", "LAK", "TEZAK", "EKSTREMNO_TEZAK"};
    private final String[] importances = {"NORMALAN", "VAZAN", "EKSTREMNO_VAZAN", "SPECIJALAN"};
    private final String[] types = {TaskItem.TYPE_ONE_TIME, TaskItem.TYPE_REPEATING};

    @Override
    protected FragmentCreateTaskBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentCreateTaskBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TasksViewModel.class);

        getBinding().spinnerDifficulty.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, difficulties));
        getBinding().spinnerImportance.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, importances));
        getBinding().spinnerType.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, types));

        setupListeners();
        setupObservers();
        viewModel.handleAction(new TasksAction.Load());
    }

    private void setupListeners() {
        getBinding().btnSaveTask.setOnClickListener(v -> submitTask());
        getBinding().btnAddCategory.setOnClickListener(v -> showCreateCategoryDialog());
        getBinding().toolbar.setNavigationOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
    }

    private void setupObservers() {
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            categories = state.getCategories();
            List<String> names = new ArrayList<>();
            for (TaskCategory category : categories) names.add(category.getName());
            getBinding().spinnerCategory.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, names));
            getBinding().btnSaveTask.setEnabled(!categories.isEmpty());
            getBinding().tvCategoryHint.setVisibility(categories.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getEffect().observe(getViewLifecycleOwner(), effect -> {
            if (effect instanceof TasksSideEffect.ShowToast) {
                String message = ((TasksSideEffect.ShowToast) effect).message;
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                if ("Zadatak je kreiran.".equals(message)) {
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void submitTask() {
        String title = getBinding().etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            getBinding().tilTitle.setError("Naziv zadatka je obavezan.");
            return;
        }
        getBinding().tilTitle.setError(null);

        if (categories.isEmpty()) {
            Toast.makeText(requireContext(), "Prvo kreirajte kategoriju.", Toast.LENGTH_SHORT).show();
            return;
        }

        TaskCategory category = categories.get(getBinding().spinnerCategory.getSelectedItemPosition());
        String difficulty = difficulties[getBinding().spinnerDifficulty.getSelectedItemPosition()];
        String importance = importances[getBinding().spinnerImportance.getSelectedItemPosition()];

        TaskItem task = new TaskItem(
                "",
                title,
                getBinding().etDescription.getText().toString().trim(),
                category.getId(),
                category.getName(),
                category.getColorHex(),
                types[getBinding().spinnerType.getSelectedItemPosition()],
                parseIntOrDefault(getBinding().etRepeatInterval.getText().toString().trim(), 1),
                "DAY",
                System.currentTimeMillis(),
                0,
                System.currentTimeMillis(),
                difficulty,
                importance,
                difficultyXp(difficulty) + importanceXp(importance),
                TaskItem.STATUS_ACTIVE,
                System.currentTimeMillis()
        );

        viewModel.handleAction(new TasksAction.CreateTask(task));
    }

    private void showCreateCategoryDialog() {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);

        EditText etName = new EditText(requireContext());
        etName.setHint("Naziv kategorije");
        layout.addView(etName);

        EditText etColor = new EditText(requireContext());
        etColor.setHint("Boja (#RRGGBB)");
        layout.addView(etColor);

        new AlertDialog.Builder(requireContext())
                .setTitle("Nova kategorija")
                .setView(layout)
                .setPositiveButton("Sačuvaj", (dialog, which) ->
                        viewModel.handleAction(new TasksAction.CreateCategory(etName.getText().toString(), etColor.getText().toString())))
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

    private int parseIntOrDefault(String raw, int def) {
        try { return Integer.parseInt(raw); } catch (Exception ignored) { return def; }
    }
}
