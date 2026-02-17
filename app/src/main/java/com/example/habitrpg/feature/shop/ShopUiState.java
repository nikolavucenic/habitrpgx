package com.example.habitrpg.feature.shop;

public interface ShopUiState {
    Data getData();

    class Data {
        public final boolean loading;
        public final int coins;
        public final String message;
        public Data(boolean loading, int coins, String message) { this.loading = loading; this.coins = coins; this.message = message; }
    }

    class ViewState implements ShopUiState {
        private final Data data;
        public ViewState(Data data) { this.data = data; }
        @Override public Data getData() { return data; }
    }

    static Data initialData() { return new Data(true, 0, ""); }
}
