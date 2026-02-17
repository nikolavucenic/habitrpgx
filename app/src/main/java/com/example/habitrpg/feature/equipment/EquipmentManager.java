package com.example.habitrpg.feature.equipment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class EquipmentManager {
    public static final String POTION_PP20 = "POTION_PP20";
    public static final String POTION_PP40 = "POTION_PP40";
    public static final String POTION_PERM5 = "POTION_PERM5";
    public static final String POTION_PERM10 = "POTION_PERM10";
    public static final String CLOTH_GLOVES = "CLOTH_GLOVES";
    public static final String CLOTH_SHIELD = "CLOTH_SHIELD";
    public static final String CLOTH_BOOTS = "CLOTH_BOOTS";
    public static final String WEAPON_SWORD = "WEAPON_SWORD";
    public static final String WEAPON_BOW = "WEAPON_BOW";

    private EquipmentManager() {}

    public static final class Bonuses {
        public final double ppMultiplier;
        public final int chanceBonus;
        public final int extraAttacks;
        public final double coinMultiplier;
        public final List<String> activeNames;

        public Bonuses(double ppMultiplier, int chanceBonus, int extraAttacks, double coinMultiplier, List<String> activeNames) {
            this.ppMultiplier = ppMultiplier;
            this.chanceBonus = chanceBonus;
            this.extraAttacks = extraAttacks;
            this.coinMultiplier = coinMultiplier;
            this.activeNames = activeNames;
        }
    }

    public static String nameOf(String id) { switch (id) {
        case POTION_PP20: return "Napitak +20% PP (jednokratno)";
        case POTION_PP40: return "Napitak +40% PP (jednokratno)";
        case POTION_PERM5: return "Napitak +5% PP (trajno)";
        case POTION_PERM10: return "Napitak +10% PP (trajno)";
        case CLOTH_GLOVES: return "Rukavice +10% snage";
        case CLOTH_SHIELD: return "Štit +10% šanse";
        case CLOTH_BOOTS: return "Čizme +40% šansa za +1 napad";
        case WEAPON_SWORD: return "Mač +5% snage";
        case WEAPON_BOW: return "Luk +5% novca";
        default: return id; }
    }

    public static boolean isInventoryToken(String raw) {
        return POTION_PP20.equals(raw) || POTION_PP40.equals(raw) || POTION_PERM5.equals(raw) || POTION_PERM10.equals(raw)
                || CLOTH_GLOVES.equals(raw) || CLOTH_SHIELD.equals(raw) || CLOTH_BOOTS.equals(raw);
    }

    public static Bonuses computeBonuses(List<String> tokens) {
        double pp = 0d; int chance = 0; int extraAttacks = 0; double coins = 0d; List<String> active = new ArrayList<>();
        for (String raw : safe(tokens)) {
            if (raw == null) continue;
            if (raw.equals("PERM_ACTIVE:" + POTION_PERM5)) { pp += 0.05; active.add(nameOf(POTION_PERM5)); }
            else if (raw.equals("PERM_ACTIVE:" + POTION_PERM10)) { pp += 0.10; active.add(nameOf(POTION_PERM10)); }
            else if (raw.equals("ONE_SHOT:" + POTION_PP20)) { pp += 0.20; active.add(nameOf(POTION_PP20)); }
            else if (raw.equals("ONE_SHOT:" + POTION_PP40)) { pp += 0.40; active.add(nameOf(POTION_PP40)); }
            else if (raw.startsWith("ACTIVE:" + CLOTH_GLOVES + ":")) { pp += 0.10; active.add(nameOf(CLOTH_GLOVES)); }
            else if (raw.startsWith("ACTIVE:" + CLOTH_SHIELD + ":")) { chance += 10; active.add(nameOf(CLOTH_SHIELD)); }
            else if (raw.startsWith("ACTIVE:" + CLOTH_BOOTS + ":")) { extraAttacks += 1; active.add(nameOf(CLOTH_BOOTS)); }
            else if (raw.startsWith("WEAPON_STATE:" + WEAPON_SWORD + ":")) { pp += 0.05; active.add("Mač"); }
            else if (raw.startsWith("WEAPON_STATE:" + WEAPON_BOW + ":")) { coins += 0.05; active.add("Luk"); }
        }
        return new Bonuses(pp, chance, extraAttacks, coins, active);
    }

    public static List<String> afterBattleConsumption(List<String> tokens) {
        List<String> next = new ArrayList<>();
        for (String raw : safe(tokens)) {
            if (raw == null) continue;
            if (raw.startsWith("ONE_SHOT:")) continue;
            if (raw.startsWith("ACTIVE:")) {
                String[] parts = raw.split(":");
                int left = parts.length == 3 ? Integer.parseInt(parts[2]) - 1 : 0;
                if (left > 0) next.add("ACTIVE:" + parts[1] + ":" + left);
                continue;
            }
            next.add(raw);
        }
        return next;
    }

    public static Map<String, Integer> inventoryCounts(List<String> tokens) {
        Map<String, Integer> map = new HashMap<>();
        for (String t : safe(tokens)) if (isInventoryToken(t)) map.put(t, map.getOrDefault(t, 0) + 1);
        return map;
    }

    public static List<String> safe(List<String> in) { return in == null ? new ArrayList<>() : in; }
}
