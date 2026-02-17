package com.example.habitrpg.feature.social;

import android.os.Handler;
import android.os.Looper;

import com.example.domain.core.Result;
import com.example.domain.model.SocialModels;
import com.example.domain.usecase.SocialUseCase;
import com.example.habitrpg.R;
import com.example.habitrpg.core.CoreViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SocialViewModel extends CoreViewModel<SocialUiState, SocialAction, SocialSideEffect> {

    private final SocialUseCase socialUseCase;

    @Inject
    public SocialViewModel(SocialUseCase socialUseCase) {
        this.socialUseCase = socialUseCase;
        state.setValue(new SocialUiState.Idle(SocialUiState.initialData()));
    }

    @Override
    public void handleAction(SocialAction action) {
        if (action instanceof SocialAction.Load) load();
        else if (action instanceof SocialAction.SearchUsers) search(((SocialAction.SearchUsers) action).query);
        else if (action instanceof SocialAction.AddFriend) act(socialUseCase.addFriend(((SocialAction.AddFriend) action).uid), R.string.social_toast_friend_added);
        else if (action instanceof SocialAction.CreateAlliance) act(socialUseCase.createAlliance(((SocialAction.CreateAlliance) action).name, idsOfFriends()), R.string.social_toast_alliance_created);
        else if (action instanceof SocialAction.AcceptInvite) act(socialUseCase.respondToInvite(((SocialAction.AcceptInvite) action).inviteId, true), R.string.social_toast_invite_accepted);
        else if (action instanceof SocialAction.DeclineInvite) act(socialUseCase.respondToInvite(((SocialAction.DeclineInvite) action).inviteId, false), R.string.social_toast_invite_declined);
        else if (action instanceof SocialAction.SendMessage) act(socialUseCase.sendMessage(((SocialAction.SendMessage) action).message), 0);
        else if (action instanceof SocialAction.StartMission) act(socialUseCase.startMission(), R.string.social_toast_mission_started);
    }

    private void load() {
        SocialUiState.Data current = getDataOrInitial();
        state.setValue(new SocialUiState.Loading(current));

        socialUseCase.getFriends().thenAccept(friendsResult -> {
            if (friendsResult instanceof Result.Error) {
                emitError(current, ((Result.Error<List<SocialModels.Friend>>) friendsResult).message);
                return;
            }
            List<SocialModels.Friend> friends = ((Result.Success<List<SocialModels.Friend>>) friendsResult).data;
            socialUseCase.getInvites().thenAccept(invitesResult -> {
                if (invitesResult instanceof Result.Error) {
                    emitError(current, ((Result.Error<List<SocialModels.AllianceInvite>>) invitesResult).message);
                    return;
                }
                List<SocialModels.AllianceInvite> invites = ((Result.Success<List<SocialModels.AllianceInvite>>) invitesResult).data;
                socialUseCase.getAlliance().thenAccept(allianceResult -> {
                    SocialModels.Alliance alliance = allianceResult instanceof Result.Success
                            ? ((Result.Success<SocialModels.Alliance>) allianceResult).data
                            : null;

                    if (alliance == null) {
                        postIdle(new SocialUiState.Data(friends, current.searchResults, invites, null, new ArrayList<>(), null));
                        return;
                    }

                    socialUseCase.getMessages().thenAccept(messagesResult -> {
                        List<SocialModels.AllianceMessage> messages = messagesResult instanceof Result.Success
                                ? ((Result.Success<List<SocialModels.AllianceMessage>>) messagesResult).data
                                : new ArrayList<>();
                        postIdle(new SocialUiState.Data(friends, current.searchResults, invites, alliance, messages, null));
                    });
                });
            });
        });
    }

    private void search(String query) {
        socialUseCase.searchUsers(query.trim()).thenAccept(result -> new Handler(Looper.getMainLooper()).post(() -> {
            SocialUiState.Data current = getDataOrInitial();
            if (result instanceof Result.Success) {
                state.setValue(new SocialUiState.Idle(new SocialUiState.Data(
                        current.friends,
                        ((Result.Success<List<SocialModels.Friend>>) result).data,
                        current.invites,
                        current.alliance,
                        current.messages,
                        null
                )));
            } else {
                sideEffect.setValue(new SocialSideEffect.ShowToast(((Result.Error<List<SocialModels.Friend>>) result).message));
            }
        }));
    }

    private void act(CompletableFuture<Result<Void>> future, int successMessageRes) {
        future.thenAccept(result -> new Handler(Looper.getMainLooper()).post(() -> {
            if (result instanceof Result.Error) sideEffect.setValue(new SocialSideEffect.ShowToast(((Result.Error<Void>) result).message));
            else if (successMessageRes != 0) sideEffect.setValue(new SocialSideEffect.ShowToast(successMessageRes));
            load();
        }));
    }

    private List<String> idsOfFriends() {
        List<String> ids = new ArrayList<>();
        for (SocialModels.Friend f : getDataOrInitial().friends) ids.add(f.uid);
        return ids;
    }

    private SocialUiState.Data getDataOrInitial() {
        SocialUiState current = state.getValue();
        return current == null ? SocialUiState.initialData() : current.getData();
    }

    private void postIdle(SocialUiState.Data data) {
        new Handler(Looper.getMainLooper()).post(() -> state.setValue(new SocialUiState.Idle(data)));
    }

    private void emitError(SocialUiState.Data base, String message) {
        new Handler(Looper.getMainLooper()).post(() -> state.setValue(new SocialUiState.Error(
                new SocialUiState.Data(base.friends, base.searchResults, base.invites, base.alliance, base.messages, message)
        )));
    }
}
