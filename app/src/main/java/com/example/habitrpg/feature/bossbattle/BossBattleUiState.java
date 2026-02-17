package com.example.habitrpg.feature.bossbattle;

public class BossBattleUiState {

    public final boolean loading;
    public final int bossNumber;
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
    public final String battleMessage;

    public BossBattleUiState(boolean loading,
                             int bossNumber,
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
                             String battleMessage) {
        this.loading = loading;
        this.bossNumber = bossNumber;
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
        this.battleMessage = battleMessage;
    }

    public static BossBattleUiState loading() {
        return new BossBattleUiState(true, 1, 200, 200, 0, 0, 5,
                false, false, 0, null, false, "");
    }
}
