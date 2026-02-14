package com.example.habitrpg.feature.profile;

public abstract class ProfileAction {
    public static class LoadProfile extends ProfileAction {
    }

    public static class OnOldPasswordChanged extends ProfileAction {
        public final String value;

        public OnOldPasswordChanged(String value) {
            this.value = value;
        }
    }

    public static class OnNewPasswordChanged extends ProfileAction {
        public final String value;

        public OnNewPasswordChanged(String value) {
            this.value = value;
        }
    }

    public static class OnConfirmPasswordChanged extends ProfileAction {
        public final String value;

        public OnConfirmPasswordChanged(String value) {
            this.value = value;
        }
    }

    public static class OnChangePasswordClicked extends ProfileAction {
    }

    public static class OnLogoutClicked extends ProfileAction {
    }
}
