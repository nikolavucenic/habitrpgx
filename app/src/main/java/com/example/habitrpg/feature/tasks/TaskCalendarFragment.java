package com.example.habitrpg.feature.tasks;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.domain.model.TaskItem;
import com.example.habitrpg.core.CoreFragment;
import com.example.habitrpg.databinding.FragmentTaskCalendarBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TaskCalendarFragment extends CoreFragment<FragmentTaskCalendarBinding> {

    private TasksViewModel viewModel;
    private TaskAdapter taskAdapter;
    private List<TaskItem> allTasks = new ArrayList<>();
    private long selectedDayMillis;

    @Override
    protected FragmentTaskCalendarBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentTaskCalendarBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TasksViewModel.class);
        selectedDayMillis = normalizeStartOfDay(System.currentTimeMillis());
        setupToolbar();
        setupList();
        setupCalendar();
        setupObservers();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.handleAction(new TasksAction.Load());
    }

    private void setupToolbar() {
        getBinding().toolbarCalendar.setNavigationOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
    }

    private void setupList() {
        taskAdapter = new TaskAdapter(new TaskAdapter.TaskStatusListener() {
            @Override public void onOpenDetails(String taskId) {
                viewModel.handleAction(new TasksAction.SelectTask(taskId));
                Navigation.findNavController(requireView()).navigate(com.example.habitrpg.R.id.nav_task_detail);
            }
            @Override public void onRequestDelete(String taskId) { confirmDelete(taskId); }
            @Override public void onSetDone(String taskId) { viewModel.handleAction(new TasksAction.ChangeStatus(taskId, TaskItem.STATUS_DONE)); }
            @Override public void onSetCanceled(String taskId) { viewModel.handleAction(new TasksAction.ChangeStatus(taskId, TaskItem.STATUS_CANCELED)); }
            @Override public void onSetPaused(String taskId) { viewModel.handleAction(new TasksAction.ChangeStatus(taskId, TaskItem.STATUS_PAUSED)); }
            @Override public void onSetActive(String taskId) { viewModel.handleAction(new TasksAction.ChangeStatus(taskId, TaskItem.STATUS_ACTIVE)); }
        });
        getBinding().rvCalendarTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        getBinding().rvCalendarTasks.setAdapter(taskAdapter);
    }

    private void setupCalendar() {
        getBinding().calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar c = Calendar.getInstance();
            c.set(year, month, dayOfMonth, 0, 0, 0);
            c.set(Calendar.MILLISECOND, 0);
            selectedDayMillis = c.getTimeInMillis();
            renderSelectedDay();
            filterTasksForSelectedDay();
        });
        renderSelectedDay();
    }

    private void confirmDelete(String taskId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Brisanje zadatka")
                .setMessage("Da li želiš da obrišeš ovaj zadatak?")
                .setPositiveButton("Obriši", (d, w) -> viewModel.handleAction(new TasksAction.DeleteTask(taskId)))
                .setNegativeButton("Otkaži", null)
                .show();
    }

    private void setupObservers() {
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            allTasks = new ArrayList<>(state.getTasks());
            filterTasksForSelectedDay();
            if (state instanceof TasksUiState.Error) {
                Toast.makeText(requireContext(), ((TasksUiState.Error) state).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderSelectedDay() {
        String date = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date(selectedDayMillis));
        getBinding().tvSelectedDay.setText("Odabrani dan: " + date);
    }

    private void filterTasksForSelectedDay() {
        long start = selectedDayMillis;
        long end = start + 24L * 60 * 60 * 1000;
        List<TaskItem> filtered = new ArrayList<>();
        for (TaskItem task : allTasks) {
            if (task.getExecuteAt() >= start && task.getExecuteAt() < end) {
                filtered.add(task);
            }
        }
        taskAdapter.submit(filtered);
        getBinding().tvEmptyCalendarTasks.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private long normalizeStartOfDay(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }
}
