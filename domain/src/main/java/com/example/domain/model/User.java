package com.example.domain.model;

import java.util.List;

public class User {
    public final String uid;
    public final String email;
    public final String username;
    public final int avatarId; // 1..5 immutable
    public final int level;
    public final String title;
    public final int pp;
    public final int xp;
    public final int coins;
    public final List<String> badges;
    public final List<String> equipment;

    public User(String uid, String email, String username, int avatarId,
                int level, String title, int pp, int xp, int coins,
                List<String> badges, List<String> equipment) {
        this.uid = uid;
        this.email = email;
        this.username = username;
        this.avatarId = avatarId;
        this.level = level;
        this.title = title;
        this.pp = pp;
        this.xp = xp;
        this.coins = coins;
        this.badges = badges;
        this.equipment = equipment;
    }
}
