package com.example.data.local.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UserCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(UserCacheEntity entity);

    @Query("SELECT * FROM user_cache WHERE user_id = :userId LIMIT 1")
    UserCacheEntity getById(String userId);

    @Query("DELETE FROM user_cache")
    void clear();
}
