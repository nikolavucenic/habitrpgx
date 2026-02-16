package com.example.habitrpg.feature.progression;

public interface ProgressionUiState {
    Data getData();

    class Data {
        public final String username;
        public final String title;
        public final int level;
        public final int avatarId;
        public final int pp;
        public final int currentXp;
        public final int requiredXp;
        public final String importancePreview;
        public final String difficultyPreview;
        public final int bossIndex;
        public final int bossHp;
        public final int bossMaxHp;
        public final int attacksLeft;
        public final int successRate;
        public final boolean equipmentActivated;
        public final int effectivePp;
        public final String equippedItem;
        public final String battleMessage;
        public final boolean battleFinished;
        public final boolean bossDefeated;
        public final int wonCoins;
        public final String wonEquipment;
        public final boolean chestOpened;
        public final boolean bossAvailable;

        public Data(String username,
                    String title,
                    int level,
                    int avatarId,
                    int pp,
                    int currentXp,
                    int requiredXp,
                    String importancePreview,
                    String difficultyPreview,
                    int bossIndex,
                    int bossHp,
                    int bossMaxHp,
                    int attacksLeft,
                    int successRate,
                    boolean equipmentActivated,
                    int effectivePp,
                    String equippedItem,
                    String battleMessage,
                    boolean battleFinished,
                    boolean bossDefeated,
                    int wonCoins,
                    String wonEquipment,
                    boolean chestOpened,
                    boolean bossAvailable) {
            this.username = username;
            this.title = title;
            this.level = level;
            this.avatarId = avatarId;
            this.pp = pp;
            this.currentXp = currentXp;
            this.requiredXp = requiredXp;
            this.importancePreview = importancePreview;
            this.difficultyPreview = difficultyPreview;
            this.bossIndex = bossIndex;
            this.bossHp = bossHp;
            this.bossMaxHp = bossMaxHp;
            this.attacksLeft = attacksLeft;
            this.successRate = successRate;
            this.equipmentActivated = equipmentActivated;
            this.effectivePp = effectivePp;
            this.equippedItem = equippedItem;
            this.battleMessage = battleMessage;
            this.battleFinished = battleFinished;
            this.bossDefeated = bossDefeated;
            this.wonCoins = wonCoins;
            this.wonEquipment = wonEquipment;
            this.chestOpened = chestOpened;
            this.bossAvailable = bossAvailable;
        }

        public Data copyWithBattle(int bossHp,
                                   int attacksLeft,
                                   int effectivePp,
                                   boolean equipmentActivated,
                                   String battleMessage,
                                   boolean battleFinished,
                                   boolean bossDefeated,
                                   int wonCoins,
                                   String wonEquipment,
                                   boolean chestOpened,
                                   boolean bossAvailable) {
            return new Data(username, title, level, avatarId, pp, currentXp, requiredXp,
                    importancePreview, difficultyPreview, bossIndex, bossHp, bossMaxHp, attacksLeft,
                    successRate, equipmentActivated, effectivePp, equippedItem, battleMessage,
                    battleFinished, bossDefeated, wonCoins, wonEquipment, chestOpened, bossAvailable);
        }
    }

    class Loading implements ProgressionUiState {
        private final Data data;

        public Loading(Data data) {
            this.data = data;
        }

        @Override
        public Data getData() {
            return data;
        }
    }

    class Success implements ProgressionUiState {
        private final Data data;

        public Success(Data data) {
            this.data = data;
        }

        @Override
        public Data getData() {
            return data;
        }
    }

    class Error implements ProgressionUiState {
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
        return new Data("Heroj", "Početnik navika", 1, 1, 0, 0, 200, "", "", 1,
                200, 200, 5, 0, false, 0, "Bez opreme",
                "Borba počinje!", false, false, 0, null, false, true);
    }
}
