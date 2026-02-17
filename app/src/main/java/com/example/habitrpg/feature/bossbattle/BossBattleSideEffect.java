package com.example.habitrpg.feature.bossbattle;

import java.util.List;

public abstract class BossBattleSideEffect {
    public static final class ShowToast extends BossBattleSideEffect {
        public final String message;
        public ShowToast(String message) { this.message = message; }
    }

    public static final class ShowEquipmentPicker extends BossBattleSideEffect {
        public final List<String> equipmentIds;
        public final List<String> labels;
        public ShowEquipmentPicker(List<String> equipmentIds, List<String> labels) {
            this.equipmentIds = equipmentIds;
            this.labels = labels;
        }
    }

    public static final class PlayBossHitAnimation extends BossBattleSideEffect {}
    public static final class PlayBossMissAnimation extends BossBattleSideEffect {}
    public static final class PlayChestShakeAnimation extends BossBattleSideEffect {}
    public static final class NavigateBack extends BossBattleSideEffect {}
}
