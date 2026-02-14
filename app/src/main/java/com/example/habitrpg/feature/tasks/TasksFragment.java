package com.example.habitrpg.feature.tasks;

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
import com.example.habitrpg.R;
import com.example.habitrpg.core.CoreFragment;
import com.example.habitrpg.databinding.FragmentTasksBinding;

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
        setupList();
        setupListeners();
        setupObservers();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.handleAction(new TasksAction.Load());
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
        getBinding().fabAddTask.setOnClickListener(v ->
                Navigation.findNavController(requireView()).navigate(R.id.action_tasks_to_create_task));
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

            taskAdapter.submit(state.getTasks());
            getBinding().tvEmpty.setVisibility(state.getTasks().isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getEffect().observe(getViewLifecycleOwner(), effect -> {
            if (effect instanceof TasksSideEffect.ShowToast) {
                Toast.makeText(requireContext(), ((TasksSideEffect.ShowToast) effect).message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
