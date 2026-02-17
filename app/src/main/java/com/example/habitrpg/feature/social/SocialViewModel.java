package com.example.habitrpg.feature.social;

import android.os.Handler;
import android.os.Looper;

import com.example.domain.core.Result;
import com.example.domain.model.SocialModels;
import com.example.domain.usecase.SocialUseCase;
import com.example.habitrpg.core.CoreViewModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SocialViewModel extends CoreViewModel<SocialUiState, SocialAction, SocialSideEffect> {

    private final SocialUseCase socialUseCase;

    @Inject
    public SocialViewModel(SocialUseCase socialUseCase) {
        this.socialUseCase = socialUseCase;
        state.setValue(SocialUiState.initial());
    }

    @Override
    public void handleAction(SocialAction action) {
        if (action instanceof SocialAction.Load) load();
        else if (action instanceof SocialAction.SearchUsers) search(((SocialAction.SearchUsers) action).query);
        else if (action instanceof SocialAction.AddFriend) act(socialUseCase.addFriend(((SocialAction.AddFriend) action).uid), "Prijatelj dodat.");
        else if (action instanceof SocialAction.CreateAlliance) act(socialUseCase.createAlliance(((SocialAction.CreateAlliance) action).name, idsOfFriends()), "Savez kreiran i pozivi poslati.");
        else if (action instanceof SocialAction.AcceptInvite) act(socialUseCase.respondToInvite(((SocialAction.AcceptInvite) action).inviteId, true), "Poziv prihvaćen.");
        else if (action instanceof SocialAction.DeclineInvite) act(socialUseCase.respondToInvite(((SocialAction.DeclineInvite) action).inviteId, false), "Poziv odbijen.");
        else if (action instanceof SocialAction.SendMessage) act(socialUseCase.sendMessage(((SocialAction.SendMessage) action).message), null);
        else if (action instanceof SocialAction.StartMission) act(socialUseCase.startMission(), "Specijalna misija je pokrenuta.");
    }

    private void load() {
        SocialUiState current = state.getValue() == null ? SocialUiState.initial() : state.getValue();
        state.setValue(new SocialUiState(true, current.friends, current.searchResults, current.invites, current.alliance, current.messages, null));

        socialUseCase.getFriends().thenAccept(friendsResult -> {
            List<SocialModels.Friend> friends = friendsResult instanceof Result.Success ? ((Result.Success<List<SocialModels.Friend>>) friendsResult).data : new ArrayList<>();
            socialUseCase.getInvites().thenAccept(invitesResult -> {
                List<SocialModels.AllianceInvite> invites = invitesResult instanceof Result.Success ? ((Result.Success<List<SocialModels.AllianceInvite>>) invitesResult).data : new ArrayList<>();
                socialUseCase.getAlliance().thenAccept(allianceResult -> {
                    SocialModels.Alliance alliance = allianceResult instanceof Result.Success ? ((Result.Success<SocialModels.Alliance>) allianceResult).data : null;
                    if (alliance == null) {
                        new Handler(Looper.getMainLooper()).post(() -> state.setValue(new SocialUiState(false, friends, current.searchResults, invites, null, new ArrayList<>(), null)));
                        return;
                    }
                    socialUseCase.getMessages().thenAccept(messagesResult -> {
                        List<SocialModels.AllianceMessage> messages = messagesResult instanceof Result.Success ? ((Result.Success<List<SocialModels.AllianceMessage>>) messagesResult).data : new ArrayList<>();
                        new Handler(Looper.getMainLooper()).post(() -> state.setValue(new SocialUiState(false, friends, current.searchResults, invites, alliance, messages, null)));
                    });
                });
            });
        });
    }

    private void search(String query) {
        socialUseCase.searchUsers(query.trim()).thenAccept(result -> new Handler(Looper.getMainLooper()).post(() -> {
            SocialUiState current = state.getValue() == null ? SocialUiState.initial() : state.getValue();
            if (result instanceof Result.Success) {
                state.setValue(new SocialUiState(false, current.friends, ((Result.Success<List<SocialModels.Friend>>) result).data, current.invites, current.alliance, current.messages, null));
            } else {
                sideEffect.setValue(new SocialSideEffect.ShowToast(((Result.Error<List<SocialModels.Friend>>) result).message));
            }
        }));
    }

    private void act(java.util.concurrent.CompletableFuture<Result<Void>> future, String successMessage) {
        future.thenAccept(result -> new Handler(Looper.getMainLooper()).post(() -> {
            if (result instanceof Result.Error) sideEffect.setValue(new SocialSideEffect.ShowToast(((Result.Error<Void>) result).message));
            else if (successMessage != null) sideEffect.setValue(new SocialSideEffect.ShowToast(successMessage));
            load();
        }));
    }

    private List<String> idsOfFriends() {
        List<String> ids = new ArrayList<>();
        SocialUiState current = state.getValue();
        if (current == null) return ids;
        for (SocialModels.Friend f : current.friends) ids.add(f.uid);
        return ids;
    }
}
