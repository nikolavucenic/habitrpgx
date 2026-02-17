package com.example.habitrpg.feature.shop;

public abstract class ShopAction {
    public static final class OnScreenStarted extends ShopAction {}
    public static final class OnBuyClicked extends ShopAction {
        public final String itemId;
        public final int cost;
        public OnBuyClicked(String itemId, int cost) { this.itemId = itemId; this.cost = cost; }
    }
}
