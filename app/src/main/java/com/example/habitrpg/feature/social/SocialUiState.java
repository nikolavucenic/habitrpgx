package com.example.habitrpg.feature.social;

import com.example.domain.model.SocialModels;

import java.util.ArrayList;
import java.util.List;

public interface SocialUiState {

    Data getData();

    class Data {
        public final List<SocialModels.Friend> friends;
        public final List<SocialModels.Friend> searchResults;
        public final List<SocialModels.AllianceInvite> invites;
        public final SocialModels.Alliance alliance;
        public final List<SocialModels.AllianceMessage> messages;
        public final String error;

        public Data(List<SocialModels.Friend> friends,
                    List<SocialModels.Friend> searchResults,
                    List<SocialModels.AllianceInvite> invites,
                    SocialModels.Alliance alliance,
                    List<SocialModels.AllianceMessage> messages,
                    String error) {
            this.friends = friends == null ? new ArrayList<>() : friends;
            this.searchResults = searchResults == null ? new ArrayList<>() : searchResults;
            this.invites = invites == null ? new ArrayList<>() : invites;
            this.alliance = alliance;
            this.messages = messages == null ? new ArrayList<>() : messages;
            this.error = error;
        }
    }

    class Idle implements SocialUiState {
        private final Data data;

        public Idle(Data data) {
            this.data = data;
        }

        @Override
        public Data getData() {
            return data;
        }
    }

    class Loading implements SocialUiState {
        private final Data data;

        public Loading(Data data) {
            this.data = data;
        }

        @Override
        public Data getData() {
            return data;
        }
    }

    class Error implements SocialUiState {
        private final Data data;

        public Error(Data data) {
            this.data = data;
        }

        @Override
        public Data getData() {
            return data;
        }
    }

    static Data initialData() {
        return new Data(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), null, new ArrayList<>(), null);
    }
}
