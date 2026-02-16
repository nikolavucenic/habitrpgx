package com.example.habitrpg.feature.progression;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.habitrpg.R;
import com.example.habitrpg.core.CoreFragment;
import com.example.habitrpg.databinding.FragmentProgressionBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProgressionFragment extends CoreFragment<FragmentProgressionBinding> implements SensorEventListener {

    private ProgressionViewModel viewModel;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastShakeTime = 0L;

    @Override
    protected FragmentProgressionBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentProgressionBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProgressionViewModel.class);
        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        getBinding().btnAttack.setOnClickListener(v -> viewModel.handleAction(new ProgressionAction.OnAttackClicked()));
        getBinding().switchEquipment.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.handleAction(new ProgressionAction.OnEquipmentToggle(isChecked))
        );

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

            bindBattle(data);

            if (state instanceof ProgressionUiState.Error) {
                getBinding().tvError.setVisibility(View.VISIBLE);
                getBinding().tvError.setText(getString(R.string.progression_error_value, ((ProgressionUiState.Error) state).getMessage()));
            } else {
                getBinding().tvError.setVisibility(View.GONE);
            }
        });

        viewModel.handleAction(new ProgressionAction.Load());
    }

    private void bindBattle(ProgressionUiState.Data data) {
        getBinding().tvBossTitle.setText(getString(R.string.progression_boss_title, data.bossIndex));
        getBinding().tvBossHp.setText(getString(R.string.progression_boss_hp_value, data.bossHp, data.bossMaxHp));
        getBinding().progressBossHp.setMax(Math.max(1, data.bossMaxHp));
        getBinding().progressBossHp.setProgress(Math.max(0, data.bossHp));

        getBinding().tvPpPower.setText(getString(R.string.progression_pp_battle_value, data.effectivePp));
        getBinding().progressPp.setMax(Math.max(1, data.effectivePp));
        getBinding().progressPp.setProgress(Math.max(0, data.effectivePp));

        getBinding().tvEquipment.setText(getString(R.string.progression_equipment_value, data.equippedItem));
        getBinding().switchEquipment.setEnabled(data.bossAvailable && !"Bez opreme".equals(data.equippedItem) && !data.battleFinished);
        if (getBinding().switchEquipment.isChecked() != data.equipmentActivated) {
            getBinding().switchEquipment.setChecked(data.equipmentActivated);
        }

        getBinding().tvSuccessChance.setText(getString(R.string.progression_success_rate, data.successRate));
        getBinding().tvAttacksLeft.setText(getString(R.string.progression_attacks_left, data.attacksLeft));
        getBinding().tvBattleMessage.setText(data.battleMessage);

        getBinding().btnAttack.setEnabled(data.bossAvailable && !data.battleFinished && data.attacksLeft > 0);

        if (data.bossAvailable && data.battleFinished) {
            getBinding().ivChest.setVisibility(View.VISIBLE);
            if (data.chestOpened) {
                showRewards(data);
            } else {
                getBinding().tvRewards.setVisibility(View.GONE);
                animateChest();
            }
        } else {
            getBinding().ivChest.setVisibility(View.GONE);
            getBinding().tvRewards.setVisibility(View.GONE);
        }
    }

    private void showRewards(ProgressionUiState.Data data) {
        getBinding().ivChest.clearAnimation();
        String equipmentSuffix = data.wonEquipment == null ? "" : ", " + data.wonEquipment;
        getBinding().tvRewards.setVisibility(View.VISIBLE);
        getBinding().tvRewards.setText(getString(R.string.progression_rewards, data.wonCoins, equipmentSuffix));
    }

    private void animateChest() {
        Animation animation = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in);
        getBinding().ivChest.startAnimation(animation);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event == null || event.values == null || event.values.length < 3) return;
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        double acceleration = Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;
        long now = System.currentTimeMillis();

        if (acceleration > 9.5f && now - lastShakeTime > 900) {
            lastShakeTime = now;
            ProgressionUiState uiState = viewModel.getState().getValue();
            if (uiState == null) return;
            if (!uiState.getData().bossAvailable) return;
            if (uiState.getData().battleFinished) {
                viewModel.handleAction(new ProgressionAction.OnOpenChest());
            } else {
                viewModel.handleAction(new ProgressionAction.OnShakeAttack());
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private int getAvatarDrawable(int avatarId) {
        if (avatarId == 2) return R.drawable.avatar_2;
        if (avatarId == 3) return R.drawable.avatar_3;
        if (avatarId == 4) return R.drawable.avatar_4;
        if (avatarId == 5) return R.drawable.avatar_5;
        return R.drawable.avatar_1;
    }
}
