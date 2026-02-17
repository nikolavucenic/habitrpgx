package com.example.habitrpg.feature.tasks;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.domain.model.TaskCategory;
import com.example.domain.model.TaskItem;
import com.example.habitrpg.core.CoreFragment;
import com.example.habitrpg.databinding.FragmentCreateTaskBinding;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CreateTaskFragment extends CoreFragment<FragmentCreateTaskBinding> {

    private TasksViewModel viewModel;
    private final List<TaskCategory> categories = new ArrayList<>();

    private final Map<String, String> frequencyMap = new LinkedHashMap<>();
    private final Map<String, String> importanceMap = new LinkedHashMap<>();
    private final Map<String, String> difficultyMap = new LinkedHashMap<>();
    private final Map<String, String> repeatUnitMap = new LinkedHashMap<>();

    private final LinkedHashMap<String, String> categoryColorMap = new LinkedHashMap<>();

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

        initMaps();
        setupDropdowns();
        setupListeners();
        setupObservers();
        syncDateTimeFields();
        viewModel.handleAction(new TasksAction.Load());
    }

    private void initMaps() {
        frequencyMap.put("Jednokratno", TaskItem.TYPE_ONE_TIME);
        frequencyMap.put("Ponavljajući", TaskItem.TYPE_REPEATING);

        importanceMap.put("Normalan", "NORMALAN");
        importanceMap.put("Važan", "VAZAN");
        importanceMap.put("Ekstremno važan", "EKSTREMNO_VAZAN");
        importanceMap.put("Specijalan", "SPECIJALAN");

        difficultyMap.put("Veoma lak", "VEOMA_LAK");
        difficultyMap.put("Lak", "LAK");
        difficultyMap.put("Težak", "TEZAK");
        difficultyMap.put("Ekstremno težak", "EKSTREMNO_TEZAK");

        repeatUnitMap.put("Dan", "DAY");
        repeatUnitMap.put("Nedelja", "WEEK");

        categoryColorMap.put("Plava", "#5B5CE2");
        categoryColorMap.put("Zelena", "#16A34A");
        categoryColorMap.put("Narandžasta", "#EA580C");
        categoryColorMap.put("Ljubičasta", "#9333EA");
        categoryColorMap.put("Crvena", "#DC2626");
        categoryColorMap.put("Tirkizna", "#0D9488");
        categoryColorMap.put("Ružičasta", "#DB2777");
        categoryColorMap.put("Siva", "#4B5563");
    }

    private void setupDropdowns() {
        bindDropdown(getBinding().actFrequency, new ArrayList<>(frequencyMap.keySet()), "Jednokratno");
        bindDropdown(getBinding().actImportance, new ArrayList<>(importanceMap.keySet()), "Normalan");
        bindDropdown(getBinding().actDifficulty, new ArrayList<>(difficultyMap.keySet()), "Lak");
        bindDropdown(getBinding().actUnit, new ArrayList<>(repeatUnitMap.keySet()), "Dan");
        getBinding().etInterval.setText("1");
    }

    private void bindDropdown(AutoCompleteTextView view, List<String> values, String selected) {
        view.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, values));
        view.setText(selected, false);
    }

    private void setupListeners() {
        getBinding().btnAddCategory.setOnClickListener(v -> showCreateCategoryDialog());
        getBinding().btnCancel.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        getBinding().btnSave.setOnClickListener(v -> submitTask());

        getBinding().actFrequency.setOnItemClickListener((parent, view, position, id) -> {
            String selected = textValue(getBinding().actFrequency.getText());
            boolean repeating = TaskItem.TYPE_REPEATING.equals(frequencyMap.get(selected));
            getBinding().cardRepeat.setVisibility(repeating ? View.VISIBLE : View.GONE);
        });

        getBinding().etTime.setOnClickListener(v -> showDateTimePicker(true));
        getBinding().etEndTime.setOnClickListener(v -> showDateTimePicker(false));
        getBinding().etStartDate.setOnClickListener(v -> showDatePicker(true));
        getBinding().etEndDate.setOnClickListener(v -> showDatePicker(false));
    }

    private void setupObservers() {
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            categories.clear();
            categories.addAll(state.getCategories());

            List<String> categoryNames = new ArrayList<>();
            for (TaskCategory category : categories) categoryNames.add(category.getName());

            bindDropdown(getBinding().actCategory, categoryNames, categoryNames.isEmpty() ? "" : categoryNames.get(0));

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
        String name = textValue(getBinding().etName.getText());
        if (name.isEmpty()) {
            getBinding().tilName.setError("Naziv zadatka je obavezan");
            return;
        }
        getBinding().tilName.setError(null);

        TaskCategory selectedCategory = findSelectedCategory(textValue(getBinding().actCategory.getText()));
        if (selectedCategory == null) {
            Toast.makeText(requireContext(), "Izaberi kategoriju", Toast.LENGTH_SHORT).show();
            return;
        }

        String frequencyLabel = textValue(getBinding().actFrequency.getText());
        String importanceLabel = textValue(getBinding().actImportance.getText());
        String difficultyLabel = textValue(getBinding().actDifficulty.getText());
        String unitLabel = textValue(getBinding().actUnit.getText());

        String frequency = getMappedOrDefault(frequencyMap, frequencyLabel, TaskItem.TYPE_ONE_TIME);
        String importance = getMappedOrDefault(importanceMap, importanceLabel, "NORMALAN");
        String difficulty = getMappedOrDefault(difficultyMap, difficultyLabel, "LAK");
        String repeatUnit = getMappedOrDefault(repeatUnitMap, unitLabel, "DAY");

        int repeatInterval = parseIntOrDefault(textValue(getBinding().etInterval.getText()), 1);
        if (TaskItem.TYPE_REPEATING.equals(frequency)) {
            if (repeatInterval <= 0) {
                Toast.makeText(requireContext(), "Interval mora biti veći od 0", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedRepeatEndDate == 0L || selectedRepeatEndDate < selectedRepeatStartDate) {
                Toast.makeText(requireContext(), "Odaberi validan datum završetka ponavljanja", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        TaskItem task = new TaskItem(
                "",
                name,
                textValue(getBinding().etDescription.getText()),
                selectedCategory.getId(),
                selectedCategory.getName(),
                selectedCategory.getColorHex(),
                frequency,
                repeatInterval,
                repeatUnit,
                selectedRepeatStartDate,
                selectedRepeatEndDate,
                selectedExecuteAt,
                difficulty,
                importance,
                0,
                TaskItem.STATUS_ACTIVE,
                System.currentTimeMillis()
        );

        viewModel.handleAction(new TasksAction.CreateTask(task));
    }

    private void showCreateCategoryDialog() {
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(24, 24, 24, 24);

        TextInputEditText etName = new TextInputEditText(requireContext());
        etName.setHint("Naziv kategorije");
        container.addView(etName);

        Spinner spinnerColor = new Spinner(requireContext());
        List<String> colorNames = new ArrayList<>(categoryColorMap.keySet());
        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                colorNames
        );
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerColor.setAdapter(colorAdapter);
        if (!colorNames.isEmpty()) {
            spinnerColor.setSelection(0);
        }
        container.addView(spinnerColor);

        new AlertDialog.Builder(requireContext())
                .setTitle("Nova kategorija")
                .setView(container)
                .setPositiveButton("Sačuvaj", (dialog, which) -> {
                    String name = textValue(etName.getText());
                    String colorName = String.valueOf(spinnerColor.getSelectedItem());
                    String colorHex = getMappedOrDefault(categoryColorMap, colorName, "#5B5CE2");
                    viewModel.handleAction(new TasksAction.CreateCategory(name, colorHex));
                })
                .setNegativeButton("Otkaži", null)
                .show();
    }

    private TaskCategory findSelectedCategory(String categoryName) {
        for (TaskCategory category : categories) {
            if (category.getName().equals(categoryName)) return category;
        }
        return categories.isEmpty() ? null : categories.get(0);
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

    private String textValue(Editable editable) {
        return editable == null ? "" : editable.toString().trim();
    }

    private String getMappedOrDefault(Map<String, String> map, String key, String fallback) {
        String value = map.get(key);
        return value == null ? fallback : value;
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
