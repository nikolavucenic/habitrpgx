package com.example.domain.progression;

public final class BossBattleCalculator {

    private static final int FIRST_BOSS_HP = 200;
    private static final int FIRST_BOSS_COINS = 200;

    private BossBattleCalculator() {
    }

    public static int hpForBoss(int bossNumber) {
        int safeBossNumber = Math.max(1, bossNumber);
        long hp = FIRST_BOSS_HP;
        for (int i = 2; i <= safeBossNumber; i++) {
            hp = hp * 2L + hp / 2L;
        }
        return (int) Math.min(Integer.MAX_VALUE, hp);
    }

    public static int coinsForBoss(int bossNumber) {
        int safeBossNumber = Math.max(1, bossNumber);
        double coins = FIRST_BOSS_COINS;
        for (int i = 2; i <= safeBossNumber; i++) {
            coins *= 1.2d;
        }
        return (int) Math.round(coins);
    }
}
