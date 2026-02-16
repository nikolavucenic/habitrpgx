package com.example.domain.progression;

import com.example.domain.model.TaskItem;

import java.util.List;
import java.util.Random;

public final class BossBattleCalculator {

    public static final int INITIAL_BOSS_HP = 200;
    public static final int ATTEMPTS_PER_BATTLE = 5;
    public static final int INITIAL_BOSS_COINS = 200;

    private BossBattleCalculator() {
    }

    public static int bossHpForIndex(int bossIndex) {
        int index = Math.max(1, bossIndex);
        int hp = INITIAL_BOSS_HP;
        for (int i = 1; i < index; i++) {
            hp = hp * 2 + hp / 2;
        }
        return hp;
    }

    public static int coinsForBossIndex(int bossIndex) {
        int index = Math.max(1, bossIndex);
        int coins = INITIAL_BOSS_COINS;
        for (int i = 1; i < index; i++) {
            coins = Math.round(coins * 1.2f);
        }
        return coins;
    }

    public static int calculateSuccessRate(List<TaskItem> tasks) {
        if (tasks == null || tasks.isEmpty()) return 0;

        int successful = 0;
        int total = 0;

        for (TaskItem task : tasks) {
            String status = task.getStatus();
            if (TaskItem.STATUS_PAUSED.equals(status) || TaskItem.STATUS_CANCELED.equals(status)) {
                continue;
            }
            total++;
            if (TaskItem.STATUS_DONE.equals(status)) {
                successful++;
            }
        }

        if (total == 0) return 0;
        return Math.round((successful * 100f) / total);
    }

    public static boolean isAttackSuccessful(int successRatePercent, Random random) {
        int chance = clampPercent(successRatePercent);
        return random.nextInt(100) < chance;
    }

    public static int reducedCoinsForHalfDamage(int baseCoins) {
        return Math.max(0, baseCoins / 2);
    }

    public static int equipmentDropChance(boolean halfReward) {
        return halfReward ? 10 : 20;
    }

    public static String rollEquipment(Random random, boolean halfReward) {
        int dropChance = equipmentDropChance(halfReward);
        if (random.nextInt(100) >= dropChance) return null;
        return random.nextInt(100) < 95 ? "Odeća" : "Oružje";
    }

    private static int clampPercent(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
