package com.example.habitrpg.feature.progression;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.habitrpg.R;
import com.example.habitrpg.core.CoreFragment;
import com.example.habitrpg.databinding.FragmentProgressionBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProgressionFragment extends CoreFragment<FragmentProgressionBinding> {

    private ProgressionViewModel viewModel;

    @Override
    protected FragmentProgressionBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentProgressionBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProgressionViewModel.class);
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            ProgressionUiState.Data data = state.getData();

            getBinding().progressXp.setMax(Math.max(1, data.requiredXp));
            getBinding().progressXp.setProgress(Math.min(data.currentXp, data.requiredXp));

            getBinding().ivAvatar.setImageResource(getAvatarDrawable(data.avatarId));
            getBinding().tvHeroNameTitle.setText(getString(R.string.progression_hero_title, data.username, data.title));
            getBinding().tvLevel.setText(getString(R.string.progression_level_value, data.level));
            getBinding().tvPp.setText(getString(R.string.progression_pp_value, data.pp));
            getBinding().tvXpValue.setText(getString(R.string.progression_xp_value, data.currentXp, data.requiredXp));
            int remaining = Math.max(0, data.requiredXp - data.currentXp);
            getBinding().tvXpNeeded.setText(getString(R.string.progression_xp_needed_value, remaining));
            getBinding().tvImportanceValues.setText(data.importancePreview);
            getBinding().tvDifficultyValues.setText(data.difficultyPreview);

            if (state instanceof ProgressionUiState.Error) {
                getBinding().tvError.setVisibility(View.VISIBLE);
                getBinding().tvError.setText(getString(R.string.progression_error_value, ((ProgressionUiState.Error) state).getMessage()));
            } else {
                getBinding().tvError.setVisibility(View.GONE);
            }
        });

        viewModel.load();
    }

    private int getAvatarDrawable(int avatarId) {
        if (avatarId == 2) return R.drawable.avatar_2;
        if (avatarId == 3) return R.drawable.avatar_3;
        if (avatarId == 4) return R.drawable.avatar_4;
        if (avatarId == 5) return R.drawable.avatar_5;
        return R.drawable.avatar_1;
    }
}
