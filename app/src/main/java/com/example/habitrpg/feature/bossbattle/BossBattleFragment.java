package com.example.habitrpg.feature.bossbattle;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;

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
    private long lastShakeTs;

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
        getBinding().btnEquipment.setOnClickListener(v -> viewModel.handleAction(new BossBattleAction.OnActivateEquipmentClicked()));
        getBinding().ivChest.setOnClickListener(v -> viewModel.handleAction(new BossBattleAction.OnShakeChestTriggered()));

        viewModel.getState().observe(getViewLifecycleOwner(), this::render);
        viewModel.getEffect().observe(getViewLifecycleOwner(), this::handleEffect);

        viewModel.handleAction(new BossBattleAction.OnScreenStarted());
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

    private void render(BossBattleUiState state) {
        getBinding().tvBossName.setText(getString(R.string.boss_name_value, state.bossNumber));
        getBinding().tvBossHp.setText(getString(R.string.boss_hp_value, state.bossCurrentHp, state.bossMaxHp));
        getBinding().tvUserPp.setText(getString(R.string.boss_pp_value, state.userPp));
        getBinding().tvChance.setText(getString(R.string.boss_chance_value, state.successChance));
        getBinding().tvAttempts.setText(getString(R.string.boss_attempts_value, state.attacksLeft));
        getBinding().tvEquipmentStatus.setText(R.string.boss_equipment_placeholder);
        getBinding().tvBattleLog.setText(state.battleMessage);

        getBinding().progressBossHp.setMax(Math.max(1, state.bossMaxHp));
        getBinding().progressBossHp.setProgress(state.bossCurrentHp);
        getBinding().progressUserPp.setMax(Math.max(1, state.bossMaxHp));
        getBinding().progressUserPp.setProgress(Math.min(state.userPp, state.bossMaxHp));

        getBinding().btnAttack.setEnabled(!state.loading && !state.battleFinished && state.attacksLeft > 0);

        boolean showChest = state.battleFinished;
        getBinding().chestContainer.setVisibility(showChest ? View.VISIBLE : View.GONE);
        getBinding().ivChest.setImageResource(state.chestOpened ? R.drawable.ic_chest_open : R.drawable.ic_chest_closed);
        getBinding().tvChestHint.setText(state.chestOpened ? getString(R.string.boss_chest_opened_hint) : getString(R.string.boss_chest_closed_hint));

        if (state.chestOpened) {
            getBinding().tvRewards.setText(getString(
                    R.string.boss_rewards_value,
                    state.earnedCoins,
                    state.earnedEquipment == null ? getString(R.string.boss_no_equipment) : state.earnedEquipment
            ));
        } else {
            getBinding().tvRewards.setText("");
        }
    }

    private void handleEffect(BossBattleSideEffect effect) {
        if (effect instanceof BossBattleSideEffect.ShowToast) {
            showToast(((BossBattleSideEffect.ShowToast) effect).message);
        } else if (effect instanceof BossBattleSideEffect.PlayBossHitAnimation) {
            getBinding().ivBoss.setImageResource(R.drawable.ic_boss_hit);
            getBinding().ivBoss.postDelayed(() -> getBinding().ivBoss.setImageResource(R.drawable.ic_boss_idle), 250);
        } else if (effect instanceof BossBattleSideEffect.PlayBossMissAnimation) {
            shakeView(getBinding().ivBoss);
        } else if (effect instanceof BossBattleSideEffect.PlayChestShakeAnimation) {
            shakeView(getBinding().ivChest);
        }
    }

    private void shakeView(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", 0f, 24f);
        animator.setInterpolator(new CycleInterpolator(4f));
        animator.setDuration(350);
        animator.start();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event == null || event.values == null || event.values.length < 3) return;
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        double force = Math.sqrt(x * x + y * y + z * z);
        long now = System.currentTimeMillis();
        if (force > 17 && now - lastShakeTs > 650) {
            lastShakeTs = now;
            BossBattleUiState state = viewModel.getState().getValue();
            if (state == null) return;
            if (state.battleFinished) {
                viewModel.handleAction(new BossBattleAction.OnShakeChestTriggered());
            } else {
                viewModel.handleAction(new BossBattleAction.OnShakeAttackTriggered());
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
