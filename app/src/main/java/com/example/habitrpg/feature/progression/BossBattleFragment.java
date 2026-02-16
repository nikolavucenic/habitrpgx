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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.habitrpg.R;
import com.example.habitrpg.core.CoreFragment;
import com.example.habitrpg.databinding.FragmentBossBattleBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BossBattleFragment extends CoreFragment<FragmentBossBattleBinding> implements SensorEventListener {

    private BossBattleViewModel viewModel;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastShakeTime;

    @Override
    protected FragmentBossBattleBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentBossBattleBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(BossBattleViewModel.class);

        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        getBinding().btnAttack.setOnClickListener(v -> viewModel.handleAction(new BossBattleAction.OnAttackClicked()));
        getBinding().switchEquipment.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.handleAction(new BossBattleAction.OnEquipmentToggle(isChecked)));

        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            BossBattleUiState.Data data = state.getData();

            getBinding().tvBossName.setText(getString(R.string.boss_battle_title_value, data.bossIndex));
            getBinding().tvBossHp.setText(getString(R.string.progression_boss_hp_value, data.bossHp, data.bossMaxHp));
            getBinding().progressBossHp.setMax(Math.max(1, data.bossMaxHp));
            getBinding().progressBossHp.setProgress(Math.max(0, data.bossHp));

            getBinding().tvPower.setText(getString(R.string.progression_pp_battle_value, data.effectivePp));
            getBinding().progressPower.setMax(Math.max(1, data.effectivePp));
            getBinding().progressPower.setProgress(Math.max(0, data.effectivePp));

            getBinding().tvHitChance.setText(getString(R.string.progression_success_rate, data.successRate));
            getBinding().tvAttemptsLeft.setText(getString(R.string.progression_attacks_left, data.attacksLeft));
            getBinding().tvEquipment.setText(getString(R.string.progression_equipment_value, data.equippedItem));
            getBinding().tvBattleMessage.setText(data.battleMessage);

            boolean canFight = data.battleAvailable && !data.battleFinished && data.attacksLeft > 0;
            getBinding().btnAttack.setEnabled(canFight);
            getBinding().switchEquipment.setEnabled(data.battleAvailable && !data.battleFinished && !"Bez opreme".equals(data.equippedItem));
            if (getBinding().switchEquipment.isChecked() != data.equipmentActivated) {
                getBinding().switchEquipment.setChecked(data.equipmentActivated);
            }

            if (data.battleFinished && data.battleAvailable) {
                getBinding().ivChest.setVisibility(View.VISIBLE);
                if (data.chestOpened) {
                    String equipmentSuffix = data.wonEquipment == null ? "" : ", " + data.wonEquipment;
                    getBinding().tvRewards.setVisibility(View.VISIBLE);
                    getBinding().tvRewards.setText(getString(R.string.progression_rewards, data.wonCoins, equipmentSuffix));
                } else {
                    getBinding().tvRewards.setVisibility(View.GONE);
                }
            } else {
                getBinding().ivChest.setVisibility(View.GONE);
                getBinding().tvRewards.setVisibility(View.GONE);
            }

            if (state instanceof BossBattleUiState.Error) {
                getBinding().tvError.setVisibility(View.VISIBLE);
                getBinding().tvError.setText(((BossBattleUiState.Error) state).getMessage());
            } else {
                getBinding().tvError.setVisibility(View.GONE);
            }
        });

        viewModel.handleAction(new BossBattleAction.Load());
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
        if (sensorManager != null) sensorManager.unregisterListener(this);
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
            BossBattleUiState current = viewModel.getState().getValue();
            if (current == null || !current.getData().battleAvailable) return;

            if (current.getData().battleFinished) {
                viewModel.handleAction(new BossBattleAction.OnOpenChest());
            } else {
                viewModel.handleAction(new BossBattleAction.OnShakeAttack());
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
