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
            getBinding().progressXp.setMax(Math.max(1, state.requiredXp));
            getBinding().progressXp.setProgress(Math.min(state.currentXp, state.requiredXp));

            getBinding().ivAvatar.setImageResource(getAvatarDrawable(state.avatarId));
            getBinding().tvHeroNameTitle.setText(getString(R.string.progression_hero_title, state.username, state.title));
            getBinding().tvLevel.setText(getString(R.string.progression_level_value, state.level));
            getBinding().tvPp.setText(getString(R.string.progression_pp_value, state.pp));
            getBinding().tvXpValue.setText(getString(R.string.progression_xp_value, state.currentXp, state.requiredXp));
            int remaining = Math.max(0, state.requiredXp - state.currentXp);
            getBinding().tvXpNeeded.setText(getString(R.string.progression_xp_needed_value, remaining));
            getBinding().tvImportanceValues.setText(state.importancePreview);
            getBinding().tvDifficultyValues.setText(state.difficultyPreview);

            if (state.error != null) {
                getBinding().tvError.setVisibility(View.VISIBLE);
                getBinding().tvError.setText(getString(R.string.progression_error_value, state.error));
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
