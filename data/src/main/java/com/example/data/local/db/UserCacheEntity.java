package com.example.data.local.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_cache")
public class UserCacheEntity {
    @PrimaryKey
    @ColumnInfo(name = "uid")
    public String uid;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "username")
    public String username;

    @ColumnInfo(name = "avatar_id")
    public int avatarId;

    @ColumnInfo(name = "level")
    public int level;

    @ColumnInfo(name = "xp")
    public int xp;
}
