package com.example.habitrpg.feature.bossbattle;

public interface BossBattleUiState {

    Data getData();

    class Data {
        public final int bossNumber;
        public final int encounterLevel;
        public final int bossMaxHp;
        public final int bossCurrentHp;
        public final int userPp;
        public final int successChance;
        public final int attacksLeft;
        public final boolean battleFinished;
        public final boolean battleWon;
        public final int earnedCoins;
        public final String earnedEquipment;
        public final boolean chestOpened;
        public final boolean rewardsApplied;
        public final String battleMessage;

        public Data(int bossNumber,
                    int encounterLevel,
                    int bossMaxHp,
                    int bossCurrentHp,
                    int userPp,
                    int successChance,
                    int attacksLeft,
                    boolean battleFinished,
                    boolean battleWon,
                    int earnedCoins,
                    String earnedEquipment,
                    boolean chestOpened,
                    boolean rewardsApplied,
                    String battleMessage) {
            this.bossNumber = bossNumber;
            this.encounterLevel = encounterLevel;
            this.bossMaxHp = bossMaxHp;
            this.bossCurrentHp = bossCurrentHp;
            this.userPp = userPp;
            this.successChance = successChance;
            this.attacksLeft = attacksLeft;
            this.battleFinished = battleFinished;
            this.battleWon = battleWon;
            this.earnedCoins = earnedCoins;
            this.earnedEquipment = earnedEquipment;
            this.chestOpened = chestOpened;
            this.rewardsApplied = rewardsApplied;
            this.battleMessage = battleMessage;
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

    class Active implements BossBattleUiState {
        private final Data data;

        public Active(Data data) {
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
        return new Data(1, 1, 200, 200, 0, 0, 5,
                false, false, 0, null, false,
                false, "");
    }
}
