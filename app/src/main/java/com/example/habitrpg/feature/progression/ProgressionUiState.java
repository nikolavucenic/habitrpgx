package com.example.habitrpg.feature.progression;

public interface ProgressionUiState {
    Data getData();

    class Data {
        public final String username;
        public final String title;
        public final int level;
        public final int avatarId;
        public final int pp;
        public final int currentXp;
        public final int requiredXp;
        public final String importancePreview;
        public final String difficultyPreview;

        public Data(String username,
                    String title,
                    int level,
                    int avatarId,
                    int pp,
                    int currentXp,
                    int requiredXp,
                    String importancePreview,
                    String difficultyPreview) {
            this.username = username;
            this.title = title;
            this.level = level;
            this.avatarId = avatarId;
            this.pp = pp;
            this.currentXp = currentXp;
            this.requiredXp = requiredXp;
            this.importancePreview = importancePreview;
            this.difficultyPreview = difficultyPreview;
        }
    }

    class Loading implements ProgressionUiState {
        private final Data data;

        public Loading(Data data) {
            this.data = data;
        }

        @Override
        public Data getData() {
            return data;
        }
    }

    class Success implements ProgressionUiState {
        private final Data data;

        public Success(Data data) {
            this.data = data;
        }

        @Override
        public Data getData() {
            return data;
        }
    }

    class Error implements ProgressionUiState {
        private final Data data;
        private final String message;

        public Error(Data data, String message) {
            this.data = data;
            this.message = message;
        }

        @Override
        public Data getData() {
            return data;
        }

        public String getMessage() {
            return message;
        }
    }

    static Data initialData() {
        return new Data("Heroj", "Početnik navika", 1, 1, 0, 0, 200, "", "");
    }
}
