package com.example.habitrpg.feature.progression;

public interface BossBattleAction {
    class Load implements BossBattleAction {}
    class OnAttackClicked implements BossBattleAction {}
    class OnShakeAttack implements BossBattleAction {}
    class OnEquipmentToggle implements BossBattleAction {
        public final boolean enabled;

        public OnEquipmentToggle(boolean enabled) {
            this.enabled = enabled;
        }
    }
    class OnOpenChest implements BossBattleAction {}
}
