package com.example.domain.model;

import java.util.ArrayList;
import java.util.List;

public class SocialModels {
    public static class Friend {
        public final String uid;
        public final String username;
        public final int avatarId;

        public Friend(String uid, String username, int avatarId) {
            this.uid = uid;
            this.username = username;
            this.avatarId = avatarId;
        }
    }

    public static class Alliance {
        public final String id;
        public final String name;
        public final String leaderId;
        public final String leaderUsername;
        public final List<Friend> members;
        public final boolean missionActive;
        public final int bossHp;
        public final int bossMaxHp;

        public Alliance(String id, String name, String leaderId, String leaderUsername, List<Friend> members,
                        boolean missionActive, int bossHp, int bossMaxHp) {
            this.id = id;
            this.name = name;
            this.leaderId = leaderId;
            this.leaderUsername = leaderUsername;
            this.members = members == null ? new ArrayList<>() : members;
            this.missionActive = missionActive;
            this.bossHp = bossHp;
            this.bossMaxHp = bossMaxHp;
        }
    }

    public static class AllianceInvite {
        public final String id;
        public final String allianceId;
        public final String allianceName;
        public final String fromUid;
        public final String fromUsername;

        public AllianceInvite(String id, String allianceId, String allianceName, String fromUid, String fromUsername) {
            this.id = id;
            this.allianceId = allianceId;
            this.allianceName = allianceName;
            this.fromUid = fromUid;
            this.fromUsername = fromUsername;
        }
    }

    public static class AllianceMessage {
        public final String id;
        public final String senderUid;
        public final String senderUsername;
        public final String message;
        public final long createdAt;

        public AllianceMessage(String id, String senderUid, String senderUsername, String message, long createdAt) {
            this.id = id;
            this.senderUid = senderUid;
            this.senderUsername = senderUsername;
            this.message = message;
            this.createdAt = createdAt;
        }
    }
}
