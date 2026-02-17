package com.example.habitrpg.feature.social;

public abstract class SocialAction {
    public static class Load extends SocialAction {}
    public static class SearchUsers extends SocialAction { public final String query; public SearchUsers(String query){this.query=query;} }
    public static class AddFriend extends SocialAction { public final String uid; public AddFriend(String uid){this.uid=uid;} }
    public static class CreateAlliance extends SocialAction { public final String name; public CreateAlliance(String name){this.name=name;} }
    public static class AcceptInvite extends SocialAction { public final String inviteId; public AcceptInvite(String inviteId){this.inviteId=inviteId;} }
    public static class DeclineInvite extends SocialAction { public final String inviteId; public DeclineInvite(String inviteId){this.inviteId=inviteId;} }
    public static class SendMessage extends SocialAction { public final String message; public SendMessage(String message){this.message=message;} }
    public static class StartMission extends SocialAction {}
}
