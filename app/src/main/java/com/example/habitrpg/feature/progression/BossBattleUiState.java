package com.example.habitrpg.feature.progression;

public interface BossBattleUiState {
    Data getData();

    class Data {
        public final int bossIndex;
        public final int bossHp;
        public final int bossMaxHp;
        public final int attacksLeft;
        public final int successRate;
        public final int effectivePp;
        public final String equippedItem;
        public final boolean equipmentActivated;
        public final String battleMessage;
        public final boolean battleFinished;
        public final int wonCoins;
        public final String wonEquipment;
        public final boolean chestOpened;
        public final boolean battleAvailable;

        public Data(int bossIndex,
                    int bossHp,
                    int bossMaxHp,
                    int attacksLeft,
                    int successRate,
                    int effectivePp,
                    String equippedItem,
                    boolean equipmentActivated,
                    String battleMessage,
                    boolean battleFinished,
                    int wonCoins,
                    String wonEquipment,
                    boolean chestOpened,
                    boolean battleAvailable) {
            this.bossIndex = bossIndex;
            this.bossHp = bossHp;
            this.bossMaxHp = bossMaxHp;
            this.attacksLeft = attacksLeft;
            this.successRate = successRate;
            this.effectivePp = effectivePp;
            this.equippedItem = equippedItem;
            this.equipmentActivated = equipmentActivated;
            this.battleMessage = battleMessage;
            this.battleFinished = battleFinished;
            this.wonCoins = wonCoins;
            this.wonEquipment = wonEquipment;
            this.chestOpened = chestOpened;
            this.battleAvailable = battleAvailable;
        }

        public Data copy(int bossHp,
                         int attacksLeft,
                         boolean equipmentActivated,
                         String battleMessage,
                         boolean battleFinished,
                         int wonCoins,
                         String wonEquipment,
                         boolean chestOpened) {
            return new Data(
                    bossIndex,
                    bossHp,
                    bossMaxHp,
                    attacksLeft,
                    successRate,
                    effectivePp,
                    equippedItem,
                    equipmentActivated,
                    battleMessage,
                    battleFinished,
                    wonCoins,
                    wonEquipment,
                    chestOpened,
                    battleAvailable
            );
        }
    }

    class Loading implements BossBattleUiState {
        private final Data data;

        public Loading(Data data) {
            this.data = data;
        }

        @Override
        public Data getData() {
            return data;
        }
    }

    class Success implements BossBattleUiState {
        private final Data data;

        public Success(Data data) {
            this.data = data;
        }

        @Override
        public Data getData() {
            return data;
        }
    }

    class Error implements BossBattleUiState {
        private final Data data;
        private final String message;

        public Error(Data data, String message) {
            this.data = data;
            this.message = message;
        }

        @Override
        public Data getData() {
            return data;
        }

        public String getMessage() {
            return message;
        }
    }

    static Data initialData() {
        return new Data(1, 200, 200, 5, 0, 0,
                "Bez opreme", false, "Spreman za boss encounter.",
                false, 0, null, false, false);
    }
}
