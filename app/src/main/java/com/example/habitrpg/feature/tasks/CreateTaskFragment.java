package com.example.habitrpg.feature.tasks;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CreateTaskFragment extends CoreFragment<FragmentCreateTaskBinding> {

    private TasksViewModel viewModel;
    private List<TaskCategory> categories = new ArrayList<>();

    private final String[] frequencyOptions = {"ONE_TIME", "REPEATING"};
    private final String[] importanceOptions = {"NORMALAN", "VAZAN", "EKSTREMNO_VAZAN", "SPECIJALAN"};
    private final String[] difficultyOptions = {"VEOMA_LAK", "LAK", "TEZAK", "EKSTREMNO_TEZAK"};
    private final String[] repeatUnits = {"DAY", "WEEK"};

    private long selectedExecuteAt = System.currentTimeMillis();
    private long selectedEndTimeAt = System.currentTimeMillis();
    private long selectedRepeatStartDate = System.currentTimeMillis();
    private long selectedRepeatEndDate = 0L;

    @Override
    protected FragmentCreateTaskBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentCreateTaskBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TasksViewModel.class);
        setupDropdowns();
        setupListeners();
        setupObservers();
        syncDateTimeFields();
        viewModel.handleAction(new TasksAction.Load());
    }

    private void setupDropdowns() {
        getBinding().actFrequency.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, frequencyOptions));
        getBinding().actImportance.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, importanceOptions));
        getBinding().actDifficulty.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, difficultyOptions));
        getBinding().actUnit.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, repeatUnits));

        getBinding().actFrequency.setText(frequencyOptions[0], false);
        getBinding().actImportance.setText(importanceOptions[0], false);
        getBinding().actDifficulty.setText(difficultyOptions[1], false);
        getBinding().actUnit.setText(repeatUnits[0], false);
        getBinding().etInterval.setText("1");
    }

    private void setupListeners() {
        getBinding().btnAddCategory.setOnClickListener(v -> showCreateCategoryDialog());
        getBinding().btnCancel.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        getBinding().btnSave.setOnClickListener(v -> submitTask());

        getBinding().actFrequency.setOnItemClickListener((parent, view, position, id) -> {
            boolean repeating = "REPEATING".equals(frequencyOptions[position]);
            getBinding().cardRepeat.setVisibility(repeating ? View.VISIBLE : View.GONE);
        });

        getBinding().etTime.setOnClickListener(v -> showDateTimePicker(true));
        getBinding().etEndTime.setOnClickListener(v -> showDateTimePicker(false));
        getBinding().etStartDate.setOnClickListener(v -> showDatePicker(true));
        getBinding().etEndDate.setOnClickListener(v -> showDatePicker(false));
    }

    private void setupObservers() {
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            categories = state.getCategories();
            List<String> categoryNames = new ArrayList<>();
            for (TaskCategory category : categories) {
                categoryNames.add(category.getName());
            }
            getBinding().actCategory.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, categoryNames));
            if (!categoryNames.isEmpty() && (getBinding().actCategory.getText() == null || getBinding().actCategory.getText().toString().isEmpty())) {
                getBinding().actCategory.setText(categoryNames.get(0), false);
            }

            boolean hasCategories = !categories.isEmpty();
            getBinding().btnSave.setEnabled(hasCategories);
            getBinding().tilCategory.setError(hasCategories ? null : "Dodaj kategoriju");
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
        String name = safeText(getBinding().etName.getText());
        if (name.isEmpty()) {
            getBinding().tilName.setError("Naziv zadatka je obavezan");
            return;
        }
        getBinding().tilName.setError(null);

        if (categories.isEmpty()) {
            Toast.makeText(requireContext(), "Prvo dodaj kategoriju", Toast.LENGTH_SHORT).show();
            return;
        }

        TaskCategory selectedCategory = findSelectedCategory(getBinding().actCategory.getText() == null ? "" : getBinding().actCategory.getText().toString());
        if (selectedCategory == null) {
            Toast.makeText(requireContext(), "Izaberi kategoriju", Toast.LENGTH_SHORT).show();
            return;
        }

        String frequency = getBinding().actFrequency.getText() == null ? "ONE_TIME" : getBinding().actFrequency.getText().toString();
        String difficulty = getBinding().actDifficulty.getText() == null ? difficultyOptions[1] : getBinding().actDifficulty.getText().toString();
        String importance = getBinding().actImportance.getText() == null ? importanceOptions[0] : getBinding().actImportance.getText().toString();

        int repeatInterval = parseIntOrDefault(safeText(getBinding().etInterval.getText()), 1);
        String repeatUnit = getBinding().actUnit.getText() == null ? "DAY" : getBinding().actUnit.getText().toString();

        TaskItem task = new TaskItem(
                "",
                name,
                safeText(getBinding().etDescription.getText()),
                selectedCategory.getId(),
                selectedCategory.getName(),
                selectedCategory.getColorHex(),
                "REPEATING".equals(frequency) ? TaskItem.TYPE_REPEATING : TaskItem.TYPE_ONE_TIME,
                repeatInterval,
                repeatUnit,
                selectedRepeatStartDate,
                selectedRepeatEndDate,
                selectedExecuteAt,
                difficulty,
                importance,
                difficultyXp(difficulty) + importanceXp(importance),
                TaskItem.STATUS_ACTIVE,
                System.currentTimeMillis()
        );

        viewModel.handleAction(new TasksAction.CreateTask(task));
    }

    private TaskCategory findSelectedCategory(String categoryName) {
        for (TaskCategory category : categories) {
            if (category.getName().equals(categoryName)) return category;
        }
        return categories.isEmpty() ? null : categories.get(0);
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

    private void showDateTimePicker(boolean start) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(start ? selectedExecuteAt : selectedEndTimeAt);

        DatePickerDialog dateDialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            Calendar picked = Calendar.getInstance();
            picked.set(year, month, dayOfMonth, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));

            TimePickerDialog timeDialog = new TimePickerDialog(requireContext(), (timePicker, hourOfDay, minute) -> {
                picked.set(Calendar.HOUR_OF_DAY, hourOfDay);
                picked.set(Calendar.MINUTE, minute);
                if (start) {
                    selectedExecuteAt = picked.getTimeInMillis();
                } else {
                    selectedEndTimeAt = picked.getTimeInMillis();
                }
                syncDateTimeFields();
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            timeDialog.show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dateDialog.show();
    }

    private void showDatePicker(boolean startDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startDate ? selectedRepeatStartDate : (selectedRepeatEndDate == 0L ? System.currentTimeMillis() : selectedRepeatEndDate));

        DatePickerDialog dateDialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            Calendar picked = Calendar.getInstance();
            picked.set(year, month, dayOfMonth, 0, 0, 0);
            picked.set(Calendar.MILLISECOND, 0);
            if (startDate) {
                selectedRepeatStartDate = picked.getTimeInMillis();
            } else {
                selectedRepeatEndDate = picked.getTimeInMillis();
            }
            syncDateTimeFields();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        dateDialog.show();
    }

    private void syncDateTimeFields() {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

        getBinding().etTime.setText(dateTimeFormat.format(new Date(selectedExecuteAt)));
        getBinding().etEndTime.setText(dateTimeFormat.format(new Date(selectedEndTimeAt)));
        getBinding().etStartDate.setText(dateFormat.format(new Date(selectedRepeatStartDate)));
        getBinding().etEndDate.setText(selectedRepeatEndDate == 0L ? "" : dateFormat.format(new Date(selectedRepeatEndDate)));
    }

    private String safeText(android.text.Editable text) {
        return text == null ? "" : text.toString().trim();
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
