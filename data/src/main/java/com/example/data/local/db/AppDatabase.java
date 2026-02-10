package com.example.data.local.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {UserCacheEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserCacheDao userCacheDao();
}
