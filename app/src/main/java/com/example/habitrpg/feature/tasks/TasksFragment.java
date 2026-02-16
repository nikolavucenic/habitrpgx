package com.example.habitrpg.feature.tasks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.domain.model.TaskItem;
import com.example.habitrpg.R;
import com.example.habitrpg.core.CoreFragment;
import com.example.habitrpg.databinding.FragmentTasksBinding;
import com.google.android.material.tabs.TabLayout;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TasksFragment extends CoreFragment<FragmentTasksBinding> {

    private TasksViewModel viewModel;
    private TaskAdapter taskAdapter;

    @Override
    protected FragmentTasksBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentTasksBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TasksViewModel.class);
        setupToolbar();
        setupTabs();
        setupList();
        setupObservers();
        setupListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.handleAction(new TasksAction.Load());
    }

    private void setupToolbar() {
        getBinding().toolbarTasks.setOnMenuItemClickListener(this::onToolbarMenuClick);
    }

    private boolean onToolbarMenuClick(MenuItem item) {
        if (item.getItemId() == R.id.action_calendar) {
            Navigation.findNavController(requireView()).navigate(R.id.action_tasks_to_calendar);
            return true;
        }
        return false;
    }

    private void setupTabs() {
        TabLayout tabs = getBinding().tabLayoutTasks;
        tabs.removeAllTabs();
        tabs.addTab(tabs.newTab().setText(getString(R.string.tasks_filter_all)), true);
        tabs.addTab(tabs.newTab().setText(getString(R.string.tasks_filter_one_time)));
        tabs.addTab(tabs.newTab().setText(getString(R.string.tasks_filter_repeating)));

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewModel.handleAction(new TasksAction.OnFilterChanged(tab.getPosition()));
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                viewModel.handleAction(new TasksAction.OnFilterChanged(tab.getPosition()));
            }
        });
    }

    private void setupList() {
        taskAdapter = new TaskAdapter(new TaskAdapter.TaskStatusListener() {
            @Override public void onSetDone(String taskId) { viewModel.handleAction(new TasksAction.ChangeStatus(taskId, TaskItem.STATUS_DONE)); }
            @Override public void onSetCanceled(String taskId) { viewModel.handleAction(new TasksAction.ChangeStatus(taskId, TaskItem.STATUS_CANCELED)); }
            @Override public void onSetPaused(String taskId) { viewModel.handleAction(new TasksAction.ChangeStatus(taskId, TaskItem.STATUS_PAUSED)); }
            @Override public void onSetActive(String taskId) { viewModel.handleAction(new TasksAction.ChangeStatus(taskId, TaskItem.STATUS_ACTIVE)); }
        });

        getBinding().rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        getBinding().rvTasks.setAdapter(taskAdapter);
    }

    private void setupListeners() {
        getBinding().fabCreateTask.setOnClickListener(v ->
                Navigation.findNavController(requireView()).navigate(R.id.action_tasks_to_create_task));
    }

    private void setupObservers() {
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            getBinding().progressBar.setVisibility(state instanceof TasksUiState.Loading ? View.VISIBLE : View.GONE);
            taskAdapter.submit(state.getFilteredTasks());
            getBinding().tvEmptyTasks.setVisibility(state.getFilteredTasks().isEmpty() ? View.VISIBLE : View.GONE);

            TabLayout tabs = getBinding().tabLayoutTasks;
            if (tabs.getSelectedTabPosition() != state.getSelectedFilter() && state.getSelectedFilter() >= 0 && state.getSelectedFilter() < tabs.getTabCount()) {
                TabLayout.Tab tab = tabs.getTabAt(state.getSelectedFilter());
                if (tab != null) tab.select();
            }

            if (state instanceof TasksUiState.Error) {
                Toast.makeText(requireContext(), ((TasksUiState.Error) state).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getEffect().observe(getViewLifecycleOwner(), effect -> {
            if (effect instanceof TasksSideEffect.ShowToast) {
                Toast.makeText(requireContext(), ((TasksSideEffect.ShowToast) effect).message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
