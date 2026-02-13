package com.example.habitrpg.feature.tasks;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.habitrpg.core.CoreFragment;
import com.example.habitrpg.databinding.FragmentTasksBinding;

public class TasksFragment extends CoreFragment<FragmentTasksBinding> {
    @Override
    protected FragmentTasksBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentTasksBinding.inflate(inflater, container, false);
    }
}
