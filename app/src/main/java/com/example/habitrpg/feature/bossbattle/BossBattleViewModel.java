package com.example.habitrpg.feature.bossbattle;

import android.os.Handler;
import android.os.Looper;

import com.example.domain.core.Result;
import com.example.domain.model.User;
import com.example.domain.progression.BossBattleCalculator;
import com.example.domain.usecase.ApplyBossBattleRewardsUseCase;
import com.example.domain.usecase.GetBossHpUseCase;
import com.example.domain.usecase.GetBossNumberUseCase;
import com.example.domain.usecase.GetCurrentUserProfileUseCase;
import com.example.domain.usecase.GetLastResolvedBossEncounterLevelUseCase;
import com.example.domain.usecase.GetStageSuccessRateUseCase;
import com.example.domain.usecase.SaveBossStateUseCase;
import com.example.domain.usecase.SaveEquipmentStateUseCase;
import com.example.domain.usecase.SaveLastResolvedBossEncounterLevelUseCase;
import com.example.habitrpg.core.CoreViewModel;
import com.example.domain.usecase.SocialUseCase;
import com.example.domain.model.SpecialMissionEvent;
import com.example.habitrpg.feature.equipment.EquipmentManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class BossBattleViewModel extends CoreViewModel<BossBattleUiState, BossBattleAction, BossBattleSideEffect> {
    private static final long ATTACK_COOLDOWN_MS = 300L;
    private final GetCurrentUserProfileUseCase getCurrentUserProfileUseCase;
    private final GetStageSuccessRateUseCase getStageSuccessRateUseCase;
    private final ApplyBossBattleRewardsUseCase applyBossBattleRewardsUseCase;
    private final GetBossNumberUseCase getBossNumberUseCase;
    private final GetBossHpUseCase getBossHpUseCase;
    private final SaveBossStateUseCase saveBossStateUseCase;
    private final SaveEquipmentStateUseCase saveEquipmentStateUseCase;
    private final GetLastResolvedBossEncounterLevelUseCase getLastResolvedBossEncounterLevelUseCase;
    private final SaveLastResolvedBossEncounterLevelUseCase saveLastResolvedBossEncounterLevelUseCase;
    private final SocialUseCase socialUseCase;
    private final Random random = new Random();
    private long lastAttackTimestamp;

    @Inject
    public BossBattleViewModel(GetCurrentUserProfileUseCase getCurrentUserProfileUseCase,
                               GetStageSuccessRateUseCase getStageSuccessRateUseCase,
                               ApplyBossBattleRewardsUseCase applyBossBattleRewardsUseCase,
                               GetBossNumberUseCase getBossNumberUseCase,
                               GetBossHpUseCase getBossHpUseCase,
                               SaveBossStateUseCase saveBossStateUseCase,
                               SaveEquipmentStateUseCase saveEquipmentStateUseCase,
                               GetLastResolvedBossEncounterLevelUseCase getLastResolvedBossEncounterLevelUseCase,
                               SaveLastResolvedBossEncounterLevelUseCase saveLastResolvedBossEncounterLevelUseCase,
                               SocialUseCase socialUseCase) {
        this.getCurrentUserProfileUseCase = getCurrentUserProfileUseCase;
        this.getStageSuccessRateUseCase = getStageSuccessRateUseCase;
        this.applyBossBattleRewardsUseCase = applyBossBattleRewardsUseCase;
        this.getBossNumberUseCase = getBossNumberUseCase;
        this.getBossHpUseCase = getBossHpUseCase;
        this.saveBossStateUseCase = saveBossStateUseCase;
        this.saveEquipmentStateUseCase = saveEquipmentStateUseCase;
        this.getLastResolvedBossEncounterLevelUseCase = getLastResolvedBossEncounterLevelUseCase;
        this.saveLastResolvedBossEncounterLevelUseCase = saveLastResolvedBossEncounterLevelUseCase;
        this.socialUseCase = socialUseCase;
        state.setValue(new BossBattleUiState.Loading(BossBattleUiState.initialData()));
    }

    @Override
    public void handleAction(BossBattleAction action) {
        if (action instanceof BossBattleAction.OnScreenStarted) loadBattle();
        else if (action instanceof BossBattleAction.OnAttackClicked || action instanceof BossBattleAction.OnShakeAttackTriggered) processAttack();
        else if (action instanceof BossBattleAction.OnShakeChestTriggered) openChest();
        else if (action instanceof BossBattleAction.OnContinueClicked) continueAfterBattle();
        else if (action instanceof BossBattleAction.OnActivateEquipmentClicked) showEquipmentPicker();
        else if (action instanceof BossBattleAction.OnEquipmentSelected) activateEquipment(((BossBattleAction.OnEquipmentSelected) action).equipmentId);
    }

    private void loadBattle() {
        BossBattleUiState.Data currentData = getDataOrInitial();
        state.setValue(new BossBattleUiState.Loading(currentData));
        getCurrentUserProfileUseCase.execute().thenAccept(profileResult ->
            getStageSuccessRateUseCase.execute().thenAccept(successResult ->
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (profileResult instanceof Result.Error) {
                        state.setValue(new BossBattleUiState.Error(currentData, ((Result.Error<User>) profileResult).message));
                        return;
                    }
                    User user = ((Result.Success<User>) profileResult).data;
                    int chance = successResult instanceof Result.Success ? ((Result.Success<Integer>) successResult).data : 0;
                    int targetBossNumber = Math.max(1, user.level - 1);
                    int savedBossNumber = getBossNumberUseCase.execute();
                    int bossNumber = savedBossNumber > 0 && savedBossNumber <= targetBossNumber ? savedBossNumber : targetBossNumber;
                    int maxHp = BossBattleCalculator.hpForBoss(bossNumber);
                    int savedHp = getBossHpUseCase.execute();
                    if (savedHp <= 0) savedHp = maxHp;
                    int currentHp = bossNumber == savedBossNumber ? Math.min(savedHp, maxHp) : maxHp;

                    EquipmentManager.Bonuses bonuses = EquipmentManager.computeBonuses(user.equipment);
                    int effectivePp = Math.max(1, (int) Math.round(user.pp * (1 + bonuses.ppMultiplier)));
                    int effectiveChance = Math.max(0, Math.min(100, chance + bonuses.chanceBonus));
                    int attacks = Math.max(1, 5 + bonuses.extraAttacks);

                    persistBossState(bossNumber, currentHp);
                    state.setValue(new BossBattleUiState.Active(new BossBattleUiState.Data(
                            bossNumber, Math.max(1, user.level - 1), maxHp, currentHp,
                            effectivePp, effectiveChance, attacks,
                            false, false, 0, null, false, false,
                            "Borba je počela. Napadni bosa!", new ArrayList<>(EquipmentManager.safe(user.equipment)),
                            bonuses.activeNames.isEmpty() ? "Aktivna oprema: nema" : "Aktivna oprema: " + String.join(", ", bonuses.activeNames)
                    )));
                })
            )
        );
    }

    private void showEquipmentPicker() {
        BossBattleUiState.Data current = getCurrentData();
        if (current == null) return;
        Map<String, Integer> counts = EquipmentManager.inventoryCounts(current.equipmentTokens);
        List<String> ids = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            ids.add(e.getKey());
            labels.add(EquipmentManager.nameOf(e.getKey()) + " (x" + e.getValue() + ")");
        }
        if (ids.isEmpty()) {
            sideEffect.setValue(new BossBattleSideEffect.ShowToast("Nemate opremu za aktivaciju."));
            return;
        }
        sideEffect.setValue(new BossBattleSideEffect.ShowEquipmentPicker(ids, labels));
    }

    private void activateEquipment(String equipmentId) {
        BossBattleUiState.Data current = getCurrentData();
        if (current == null || equipmentId == null) return;
        List<String> nextTokens = new ArrayList<>(current.equipmentTokens);
        if (!nextTokens.remove(equipmentId)) {
            sideEffect.setValue(new BossBattleSideEffect.ShowToast("Oprema nije dostupna."));
            return;
        }
        if (EquipmentManager.POTION_PP20.equals(equipmentId)) nextTokens.add("ONE_SHOT:" + equipmentId);
        else if (EquipmentManager.POTION_PP40.equals(equipmentId)) nextTokens.add("ONE_SHOT:" + equipmentId);
        else if (EquipmentManager.POTION_PERM5.equals(equipmentId)) nextTokens.add("PERM_ACTIVE:" + equipmentId);
        else if (EquipmentManager.POTION_PERM10.equals(equipmentId)) nextTokens.add("PERM_ACTIVE:" + equipmentId);
        else if (EquipmentManager.CLOTH_GLOVES.equals(equipmentId) || EquipmentManager.CLOTH_SHIELD.equals(equipmentId) || EquipmentManager.CLOTH_BOOTS.equals(equipmentId)) nextTokens.add("ACTIVE:" + equipmentId + ":2");

        saveEquipmentStateUseCase.execute(nextTokens, -1).thenAccept(result -> new Handler(Looper.getMainLooper()).post(() -> {
            if (result instanceof Result.Error) {
                sideEffect.setValue(new BossBattleSideEffect.ShowToast(((Result.Error<Void>) result).message));
                return;
            }
            sideEffect.setValue(new BossBattleSideEffect.ShowToast("Oprema aktivirana."));
            loadBattle();
        }));
    }

    private void processAttack() {
        long now = System.currentTimeMillis();
        if (now - lastAttackTimestamp < ATTACK_COOLDOWN_MS) return;
        BossBattleUiState.Data d = getCurrentData();
        if (d == null || d.battleFinished || d.attacksLeft <= 0) return;
        lastAttackTimestamp = now;

        int attacksLeft = d.attacksLeft - 1;
        boolean hit = random.nextInt(100) < d.successChance;
        int nextHp = d.bossCurrentHp;
        String message;
        if (hit) { nextHp = Math.max(0, nextHp - d.userPp); message = "Pogodak!"; sideEffect.setValue(new BossBattleSideEffect.PlayBossHitAnimation()); socialUseCase.trackMissionEvent(SpecialMissionEvent.REGULAR_BOSS_HIT, 1); }
        else { message = "Promašaj!"; sideEffect.setValue(new BossBattleSideEffect.PlayBossMissAnimation()); }

        boolean finished = nextHp <= 0 || attacksLeft == 0;
        boolean won = finished && nextHp <= 0;
        int earnedCoins = 0; String earnedEquipment = null;
        if (finished) {
            Reward reward = calculateReward(d.bossNumber, d.bossMaxHp, nextHp, won, d.equipmentTokens);
            earnedCoins = reward.coins; earnedEquipment = reward.equipment;
            persistAfterBattle(d.bossNumber, won, nextHp); persistResolvedEncounterLevel(d.encounterLevel);
            saveEquipmentStateUseCase.execute(EquipmentManager.afterBattleConsumption(d.equipmentTokens), -1);
            message = won ? "Boss poražen!" : "Borba završena.";
        } else persistBossState(d.bossNumber, nextHp);

        state.setValue(new BossBattleUiState.Active(new BossBattleUiState.Data(d.bossNumber,d.encounterLevel,d.bossMaxHp,nextHp,d.userPp,d.successChance,attacksLeft,finished,won,earnedCoins,earnedEquipment,false,false,message,d.equipmentTokens,d.activeEquipmentSummary)));
    }

    private Reward calculateReward(int bossNumber, int bossMaxHp, int remainingHp, boolean won, List<String> equipmentTokens) {
        int coins = BossBattleCalculator.coinsForBoss(bossNumber);
        int chancePercent = 20;
        EquipmentManager.Bonuses bonuses = EquipmentManager.computeBonuses(equipmentTokens);
        coins = Math.round(coins * (float) (1 + bonuses.coinMultiplier));
        if (!won) {
            int damaged = bossMaxHp - remainingHp;
            if (damaged >= bossMaxHp / 2) { coins = Math.round(coins / 2f); chancePercent = 10; }
            else { coins = 0; chancePercent = 0; }
        }
        String equipment = null;
        if (random.nextInt(100) < chancePercent) {
            int roll = random.nextInt(100);
            if (roll < 45) equipment = random.nextBoolean() ? EquipmentManager.CLOTH_GLOVES : EquipmentManager.CLOTH_SHIELD;
            else if (roll < 90) equipment = EquipmentManager.CLOTH_BOOTS;
            else equipment = random.nextBoolean() ? EquipmentManager.WEAPON_SWORD : EquipmentManager.WEAPON_BOW;
        }
        return new Reward(coins, equipment);
    }

    private void openChest() {
        BossBattleUiState.Data current = getCurrentData();
        if (current == null || !current.battleFinished || current.chestOpened) return;
        sideEffect.setValue(new BossBattleSideEffect.PlayChestShakeAnimation());
        state.setValue(new BossBattleUiState.Active(copyData(current, true, current.rewardsApplied, current.battleWon ? "Kovčeg je otvoren!" : "Utešna nagrada.")));
        if (!current.rewardsApplied) applyRewards(current);
    }

    private void applyRewards(BossBattleUiState.Data currentData) {
        applyBossBattleRewardsUseCase.execute(currentData.earnedCoins, currentData.earnedEquipment).thenAccept(result -> new Handler(Looper.getMainLooper()).post(() -> {
            BossBattleUiState.Data latest = getCurrentData(); if (latest == null) return;
            if (result instanceof Result.Error) { sideEffect.setValue(new BossBattleSideEffect.ShowToast(((Result.Error<Void>) result).message)); return; }
            state.setValue(new BossBattleUiState.Active(copyData(latest, latest.chestOpened, true, latest.battleMessage)));
        }));
    }

    private void continueAfterBattle() { BossBattleUiState.Data c = getCurrentData(); if (c != null && c.chestOpened && c.rewardsApplied) sideEffect.setValue(new BossBattleSideEffect.NavigateBack()); }
    private void persistAfterBattle(int currentBossNumber, boolean won, int remainingHp) { persistBossState(won ? currentBossNumber + 1 : currentBossNumber, won ? BossBattleCalculator.hpForBoss(currentBossNumber + 1) : remainingHp); }
    private void persistBossState(int bossNumber, int hp) { saveBossStateUseCase.execute(bossNumber, hp); }
    private void persistResolvedEncounterLevel(int encounterLevel) { int lvl=Math.max(1, encounterLevel); if (lvl>getLastResolvedBossEncounterLevelUseCase.execute()) saveLastResolvedBossEncounterLevelUseCase.execute(lvl); }
    private BossBattleUiState.Data getCurrentData() { BossBattleUiState c=state.getValue(); return c!=null?c.getData():null; }
    private BossBattleUiState.Data getDataOrInitial() { BossBattleUiState.Data d=getCurrentData(); return d!=null?d:BossBattleUiState.initialData(); }
    private BossBattleUiState.Data copyData(BossBattleUiState.Data c, boolean chestOpened, boolean rewardsApplied, String message) {
        return new BossBattleUiState.Data(c.bossNumber,c.encounterLevel,c.bossMaxHp,c.bossCurrentHp,c.userPp,c.successChance,c.attacksLeft,c.battleFinished,c.battleWon,c.earnedCoins,c.earnedEquipment,chestOpened,rewardsApplied,message,c.equipmentTokens,c.activeEquipmentSummary);
    }
    private static class Reward { final int coins; final String equipment; Reward(int coins, String equipment){this.coins=coins;this.equipment=equipment;} }
}
