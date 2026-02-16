package com.example.habitrpg.feature.tasks;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.domain.model.TaskCategory;
import com.example.domain.model.TaskItem;
import com.example.habitrpg.core.CoreFragment;
import com.example.habitrpg.databinding.FragmentTaskDetailBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TaskDetailFragment extends CoreFragment<FragmentTaskDetailBinding> {

    private TasksViewModel viewModel;
    private long selectedExecuteAt;

    @Override
    protected FragmentTaskDetailBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentTaskDetailBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TasksViewModel.class);
        getBinding().toolbarTaskDetail.setNavigationOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        getBinding().etTime.setOnClickListener(v -> showDateTimePicker());

        getBinding().btnSaveChanges.setOnClickListener(v -> submitUpdate());
        getBinding().btnDeleteTask.setOnClickListener(v -> confirmDelete());

        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            TaskItem selected = state.getSelectedTask();
            if (selected == null) return;
            bindTask(selected, state.getCategories());
        });

        viewModel.getEffect().observe(getViewLifecycleOwner(), effect -> {
            if (effect instanceof TasksSideEffect.ShowToast) {
                String msg = ((TasksSideEffect.ShowToast) effect).message;
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                if ("Zadatak je obrisan.".equals(msg) || "Zadatak je izmenjen.".equals(msg)) {
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void bindTask(TaskItem task, List<TaskCategory> categories) {
        getBinding().etName.setText(task.getTitle());
        getBinding().etDescription.setText(task.getDescription());
        selectedExecuteAt = task.getExecuteAt();
        getBinding().etTime.setText(new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(new Date(selectedExecuteAt)));

        List<String> categoryNames = new ArrayList<>();
        for (TaskCategory c : categories) categoryNames.add(c.getName());
        getBinding().actCategory.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, categoryNames));
        getBinding().actCategory.setText(task.getCategoryName(), false);
    }

    private void submitUpdate() {
        TasksUiState state = viewModel.getState().getValue();
        if (state == null || state.getSelectedTask() == null) return;
        TaskItem current = state.getSelectedTask();

        String categoryName = String.valueOf(getBinding().actCategory.getText());
        TaskCategory selectedCategory = null;
        for (TaskCategory c : state.getCategories()) {
            if (c.getName().equals(categoryName)) {
                selectedCategory = c;
                break;
            }
        }
        if (selectedCategory == null && !state.getCategories().isEmpty()) selectedCategory = state.getCategories().get(0);
        if (selectedCategory == null) {
            Toast.makeText(requireContext(), "Nema dostupnih kategorija", Toast.LENGTH_SHORT).show();
            return;
        }

        TaskItem updated = new TaskItem(
                current.getId(),
                String.valueOf(getBinding().etName.getText()).trim(),
                String.valueOf(getBinding().etDescription.getText()).trim(),
                selectedCategory.getId(),
                selectedCategory.getName(),
                selectedCategory.getColorHex(),
                current.getType(),
                current.getRepeatInterval(),
                current.getRepeatUnit(),
                current.getRepeatStartAt(),
                current.getRepeatEndAt(),
                selectedExecuteAt,
                current.getDifficulty(),
                current.getImportance(),
                current.getXpValue(),
                current.getStatus(),
                current.getCreatedAt()
        );

        viewModel.handleAction(new TasksAction.UpdateTask(updated));
    }

    private void confirmDelete() {
        TasksUiState state = viewModel.getState().getValue();
        if (state == null || state.getSelectedTask() == null) return;
        String taskId = state.getSelectedTask().getId();
        new AlertDialog.Builder(requireContext())
                .setTitle("Brisanje zadatka")
                .setMessage("Da li želiš da obrišeš zadatak?")
                .setPositiveButton("Obriši", (d, w) -> viewModel.handleAction(new TasksAction.DeleteTask(taskId)))
                .setNegativeButton("Otkaži", null)
                .show();
    }

    private void showDateTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selectedExecuteAt);

        DatePickerDialog dateDialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            Calendar picked = Calendar.getInstance();
            picked.set(year, month, dayOfMonth, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));

            TimePickerDialog timeDialog = new TimePickerDialog(requireContext(), (timePicker, hourOfDay, minute) -> {
                picked.set(Calendar.HOUR_OF_DAY, hourOfDay);
                picked.set(Calendar.MINUTE, minute);
                selectedExecuteAt = picked.getTimeInMillis();
                getBinding().etTime.setText(new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(new Date(selectedExecuteAt)));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            timeDialog.show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dateDialog.show();
    }
}
