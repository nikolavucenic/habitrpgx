package com.example.habitrpg.feature.social;

import com.example.domain.model.SocialModels;

import java.util.ArrayList;
import java.util.List;

public class SocialUiState {
    public final boolean loading;
    public final List<SocialModels.Friend> friends;
    public final List<SocialModels.Friend> searchResults;
    public final List<SocialModels.AllianceInvite> invites;
    public final SocialModels.Alliance alliance;
    public final List<SocialModels.AllianceMessage> messages;
    public final String error;

    public SocialUiState(boolean loading,
                         List<SocialModels.Friend> friends,
                         List<SocialModels.Friend> searchResults,
                         List<SocialModels.AllianceInvite> invites,
                         SocialModels.Alliance alliance,
                         List<SocialModels.AllianceMessage> messages,
                         String error) {
        this.loading = loading;
        this.friends = friends == null ? new ArrayList<>() : friends;
        this.searchResults = searchResults == null ? new ArrayList<>() : searchResults;
        this.invites = invites == null ? new ArrayList<>() : invites;
        this.alliance = alliance;
        this.messages = messages == null ? new ArrayList<>() : messages;
        this.error = error;
    }

    public static SocialUiState initial() {
        return new SocialUiState(false, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), null, new ArrayList<>(), null);
    }
}
