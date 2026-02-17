package com.example.domain.usecase;

import com.example.domain.core.Result;
import com.example.domain.model.SocialModels;
import com.example.domain.repository.SocialRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SocialUseCase {
    private final SocialRepository repository;

    public SocialUseCase(SocialRepository repository) {
        this.repository = repository;
    }

    public CompletableFuture<Result<List<SocialModels.Friend>>> getFriends() { return repository.getFriends(); }
    public CompletableFuture<Result<List<SocialModels.Friend>>> searchUsers(String query) { return repository.searchUsers(query); }
    public CompletableFuture<Result<Void>> addFriend(String uid) { return repository.addFriend(uid); }
    public CompletableFuture<Result<SocialModels.Alliance>> getAlliance() { return repository.getAlliance(); }
    public CompletableFuture<Result<Void>> createAlliance(String name, List<String> invited) { return repository.createAlliance(name, invited); }
    public CompletableFuture<Result<List<SocialModels.AllianceInvite>>> getInvites() { return repository.getInvites(); }
    public CompletableFuture<Result<Void>> respondToInvite(String inviteId, boolean accept) { return repository.respondToInvite(inviteId, accept); }
    public CompletableFuture<Result<List<SocialModels.AllianceMessage>>> getMessages() { return repository.getAllianceMessages(); }
    public CompletableFuture<Result<Void>> sendMessage(String message) { return repository.sendAllianceMessage(message); }
    public CompletableFuture<Result<Void>> startMission() { return repository.startSpecialMission(); }
    public CompletableFuture<Result<Void>> trackMissionEvent(String eventType, int amount) { return repository.trackMissionEvent(eventType, amount); }
}
