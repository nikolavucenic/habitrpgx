package com.example.habitrpg.feature.shop;

import android.os.Handler;
import android.os.Looper;

import com.example.domain.core.Result;
import com.example.domain.model.User;
import com.example.domain.usecase.GetCurrentUserProfileUseCase;
import com.example.domain.usecase.PurchaseEquipmentUseCase;
import com.example.domain.usecase.SocialUseCase;
import com.example.domain.model.SpecialMissionEvent;
import com.example.habitrpg.core.CoreViewModel;
import com.example.habitrpg.feature.equipment.EquipmentManager;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ShopViewModel extends CoreViewModel<ShopUiState, ShopAction, String> {
    private final GetCurrentUserProfileUseCase getCurrentUserProfileUseCase;
    private final PurchaseEquipmentUseCase purchaseEquipmentUseCase;
    private final SocialUseCase socialUseCase;

    @Inject
    public ShopViewModel(GetCurrentUserProfileUseCase getCurrentUserProfileUseCase,
                         PurchaseEquipmentUseCase purchaseEquipmentUseCase,
                         SocialUseCase socialUseCase) {
        this.getCurrentUserProfileUseCase = getCurrentUserProfileUseCase;
        this.purchaseEquipmentUseCase = purchaseEquipmentUseCase;
        this.socialUseCase = socialUseCase;
        state.setValue(new ShopUiState.ViewState(ShopUiState.initialData()));
    }

    @Override
    public void handleAction(ShopAction action) {
        if (action instanceof ShopAction.OnScreenStarted) load();
        else if (action instanceof ShopAction.OnBuyClicked) {
            ShopAction.OnBuyClicked buy = (ShopAction.OnBuyClicked) action;
            purchaseEquipmentUseCase.execute(buy.itemId, buy.cost).thenAccept(result -> new Handler(Looper.getMainLooper()).post(() -> {
                if (result instanceof Result.Error) sideEffect.setValue(((Result.Error<Void>) result).message);
                else {
                    sideEffect.setValue("Kupljeno: " + EquipmentManager.nameOf(buy.itemId));
                    socialUseCase.trackMissionEvent(SpecialMissionEvent.SHOP_PURCHASE, 1);
                }
                load();
            }));
        }
    }

    private void load() {
        ShopUiState.Data data = state.getValue() != null ? state.getValue().getData() : ShopUiState.initialData();
        state.setValue(new ShopUiState.ViewState(new ShopUiState.Data(true, data.coins, data.message)));
        getCurrentUserProfileUseCase.execute().thenAccept(result -> new Handler(Looper.getMainLooper()).post(() -> {
            if (result instanceof Result.Success) {
                User user = ((Result.Success<User>) result).data;
                state.setValue(new ShopUiState.ViewState(new ShopUiState.Data(false, user.coins, "")));
            } else {
                state.setValue(new ShopUiState.ViewState(new ShopUiState.Data(false, data.coins, ((Result.Error<User>) result).message)));
            }
        }));
    }
}
