package com.example.habitrpg;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.habitrpg.feature.dashboard.DashboardViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DashboardFragment extends Fragment {

    private DashboardViewModel viewModel;

    private TextView tvHero;
    private TextView tvLevel;
    private TextView tvPp;
    private TextView tvXp;
    private TextView tvNeeded;
    private TextView tvImportance;
    private TextView tvDifficulty;
    private TextView tvError;
    private ProgressBar progressXp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvHero = view.findViewById(R.id.tv_hero_name_title);
        tvLevel = view.findViewById(R.id.tv_level);
        tvPp = view.findViewById(R.id.tv_pp);
        tvXp = view.findViewById(R.id.tv_xp_value);
        tvNeeded = view.findViewById(R.id.tv_xp_needed);
        tvImportance = view.findViewById(R.id.tv_importance_values);
        tvDifficulty = view.findViewById(R.id.tv_difficulty_values);
        tvError = view.findViewById(R.id.tv_error);
        progressXp = view.findViewById(R.id.progress_xp);

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            progressXp.setMax(Math.max(1, state.requiredXp));
            progressXp.setProgress(Math.min(state.currentXp, state.requiredXp));

            tvHero.setText(getString(R.string.dashboard_hero_title, state.username, state.title));
            tvLevel.setText(getString(R.string.dashboard_level_value, state.level));
            tvPp.setText(getString(R.string.dashboard_pp_value, state.pp));
            tvXp.setText(getString(R.string.dashboard_xp_value, state.currentXp, state.requiredXp));
            int remaining = Math.max(0, state.requiredXp - state.currentXp);
            tvNeeded.setText(getString(R.string.dashboard_xp_needed_value, remaining));
            tvImportance.setText(state.importancePreview);
            tvDifficulty.setText(state.difficultyPreview);

            if (state.error != null) {
                tvError.setVisibility(View.VISIBLE);
                tvError.setText(getString(R.string.dashboard_error_value, state.error));
            } else {
                tvError.setVisibility(View.GONE);
            }
        });

        viewModel.loadProgression();
    }
}
