package com.example.habitrpg.feature.social;

import androidx.annotation.StringRes;

public abstract class SocialSideEffect {
    public static class ShowToast extends SocialSideEffect {
        public final String message;
        @StringRes public final int messageRes;

        public ShowToast(String message) {
            this.message = message;
            this.messageRes = 0;
        }

        public ShowToast(@StringRes int messageRes) {
            this.messageRes = messageRes;
            this.message = null;
        }
    }
}
