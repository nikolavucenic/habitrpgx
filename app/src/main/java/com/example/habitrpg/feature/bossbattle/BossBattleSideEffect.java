package com.example.habitrpg.feature.bossbattle;

public abstract class BossBattleSideEffect {
    public static final class ShowToast extends BossBattleSideEffect {
        public final String message;

        public ShowToast(String message) {
            this.message = message;
        }
    }

    public static final class PlayBossHitAnimation extends BossBattleSideEffect {}
    public static final class PlayBossMissAnimation extends BossBattleSideEffect {}
    public static final class PlayChestShakeAnimation extends BossBattleSideEffect {}
    public static final class NavigateBack extends BossBattleSideEffect {}
}
