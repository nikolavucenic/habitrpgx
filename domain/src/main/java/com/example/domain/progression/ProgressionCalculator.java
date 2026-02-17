package com.example.domain.progression;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ProgressionCalculator {

    public static final int BASE_XP_FOR_FIRST_LEVEL = 200;
    public static final int BASE_PP_REWARD = 40;

    private ProgressionCalculator() {}

    public static int requiredXpForLevel(int level) {
        if (level <= 1) {
            return BASE_XP_FOR_FIRST_LEVEL;
        }

        int requiredXp = BASE_XP_FOR_FIRST_LEVEL;
        for (int current = 2; current <= level; current++) {
            requiredXp = (int) Math.round(requiredXp * 2.5);
        }
        return requiredXp;
    }

    public static int ppRewardForLevel(int level) {
        if (level <= 1) {
            return BASE_PP_REWARD;
        }

        int ppReward = BASE_PP_REWARD;
        for (int current = 2; current <= level; current++) {
            ppReward = (int) Math.round(ppReward + (ppReward * 0.75));
        }
        return ppReward;
    }

    public static int scaledXpValue(int baseXp, int passedLevels) {
        int xpValue = baseXp;
        for (int i = 0; i < passedLevels; i++) {
            xpValue = (int) Math.round(xpValue + (xpValue * 0.5));
        }
        return xpValue;
    }

    public static String titleForLevel(int level) {
        if (level <= 1) return "Početnik navika";
        if (level == 2) return "Čuvar discipline";
        if (level == 3) return "Gospodar ritma";
        return "Legendarni strateg navika";
    }

    public static Map<String, Integer> importanceXpByPassedLevel(int passedLevels) {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("Normalan", scaledXpValue(1, passedLevels));
        map.put("Važan", scaledXpValue(3, passedLevels));
        map.put("Ekstremno važan", scaledXpValue(10, passedLevels));
        map.put("Specijalan", scaledXpValue(100, passedLevels));
        return map;
    }

    public static Map<String, Integer> difficultyXpByPassedLevel(int passedLevels) {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("Lako", scaledXpValue(2, passedLevels));
        map.put("Srednje", scaledXpValue(5, passedLevels));
        map.put("Teško", scaledXpValue(9, passedLevels));
        return map;
    }
}
