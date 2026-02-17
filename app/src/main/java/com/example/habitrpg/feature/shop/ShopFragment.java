package com.example.habitrpg.feature.shop;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.example.habitrpg.core.CoreFragment;
import com.example.habitrpg.databinding.FragmentShopBinding;
import com.example.habitrpg.feature.equipment.EquipmentManager;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ShopFragment extends CoreFragment<FragmentShopBinding> {
    private ShopViewModel viewModel;

    @Override
    protected FragmentShopBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentShopBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(View view, android.os.Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ShopViewModel.class);
        bindActions();
        viewModel.getState().observe(getViewLifecycleOwner(), this::render);
        viewModel.getEffect().observe(getViewLifecycleOwner(), msg -> Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show());
        viewModel.handleAction(new ShopAction.OnScreenStarted());
    }

    private void bindActions() {
        getBinding().btnBuyPotion20.setOnClickListener(v -> buy(EquipmentManager.POTION_PP20, 50));
        getBinding().btnBuyPotion40.setOnClickListener(v -> buy(EquipmentManager.POTION_PP40, 70));
        getBinding().btnBuyPotionPerm5.setOnClickListener(v -> buy(EquipmentManager.POTION_PERM5, 200));
        getBinding().btnBuyPotionPerm10.setOnClickListener(v -> buy(EquipmentManager.POTION_PERM10, 1000));
        getBinding().btnBuyGloves.setOnClickListener(v -> buy(EquipmentManager.CLOTH_GLOVES, 60));
        getBinding().btnBuyShield.setOnClickListener(v -> buy(EquipmentManager.CLOTH_SHIELD, 60));
        getBinding().btnBuyBoots.setOnClickListener(v -> buy(EquipmentManager.CLOTH_BOOTS, 80));
    }

    private void buy(String itemId, int pricePercent) {
        ShopUiState state = viewModel.getState().getValue();
        int bossBase = 100;
        int cost = (int) Math.round(bossBase * (pricePercent / 100f));
        if (state != null && state.getData().coins < cost) {
            Toast.makeText(getContext(), getString(com.example.habitrpg.R.string.shop_not_enough_coins), Toast.LENGTH_SHORT).show();
            return;
        }
        viewModel.handleAction(new ShopAction.OnBuyClicked(itemId, cost));
    }

    private void render(ShopUiState state) {
        getBinding().tvCoins.setText(getString(com.example.habitrpg.R.string.shop_coins_value, state.getData().coins));
        getBinding().tvMessage.setText(state.getData().message);
        getBinding().progressBar.setVisibility(state.getData().loading ? View.VISIBLE : View.GONE);
    }
}
