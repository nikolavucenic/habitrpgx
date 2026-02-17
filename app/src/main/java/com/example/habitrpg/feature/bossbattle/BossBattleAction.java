package com.example.habitrpg.feature.bossbattle;

public abstract class BossBattleAction {
    public static final class OnScreenStarted extends BossBattleAction {}
    public static final class OnAttackClicked extends BossBattleAction {}
    public static final class OnShakeAttackTriggered extends BossBattleAction {}
    public static final class OnShakeChestTriggered extends BossBattleAction {}
    public static final class OnContinueClicked extends BossBattleAction {}
    public static final class OnActivateEquipmentClicked extends BossBattleAction {}
    public static final class OnEquipmentSelected extends BossBattleAction {
        public final String equipmentId;
        public OnEquipmentSelected(String equipmentId) { this.equipmentId = equipmentId; }
    }
}
