package com.example.domain.repository;

import com.example.domain.core.Result;
import com.example.domain.model.SocialModels;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SocialRepository {
    CompletableFuture<Result<List<SocialModels.Friend>>> getFriends();
    CompletableFuture<Result<List<SocialModels.Friend>>> searchUsers(String query);
    CompletableFuture<Result<Void>> addFriend(String targetUid);

    CompletableFuture<Result<SocialModels.Alliance>> getAlliance();
    CompletableFuture<Result<Void>> createAlliance(String name, List<String> invitedFriendUids);
    CompletableFuture<Result<List<SocialModels.AllianceInvite>>> getInvites();
    CompletableFuture<Result<Void>> respondToInvite(String inviteId, boolean accept);

    CompletableFuture<Result<List<SocialModels.AllianceMessage>>> getAllianceMessages();
    CompletableFuture<Result<Void>> sendAllianceMessage(String message);
    CompletableFuture<Result<Void>> startSpecialMission();
    CompletableFuture<Result<Void>> trackMissionEvent(String eventType, int amount);
}
