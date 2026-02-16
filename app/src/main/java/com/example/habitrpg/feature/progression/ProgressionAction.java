package com.example.habitrpg.feature.progression;

public interface ProgressionAction {
    class Load implements ProgressionAction {}
    class OnAttackClicked implements ProgressionAction {}
    class OnShakeAttack implements ProgressionAction {}
    class OnEquipmentToggle implements ProgressionAction {
        public final boolean enabled;

        public OnEquipmentToggle(boolean enabled) {
            this.enabled = enabled;
        }
    }
    class OnOpenChest implements ProgressionAction {}
}
