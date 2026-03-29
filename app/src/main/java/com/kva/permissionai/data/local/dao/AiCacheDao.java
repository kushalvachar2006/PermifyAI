package com.kva.permissionai.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.kva.permissionai.data.local.entities.AiCacheEntity;

@Dao
public interface AiCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void cacheAiResponse(AiCacheEntity cache);

    @Query("SELECT * FROM ai_cache WHERE queryKey = :queryKey LIMIT 1")
    AiCacheEntity getCachedResponse(String queryKey);

    @Query("DELETE FROM ai_cache WHERE timestamp < :expiryTime")
    void deleteOldCache(long expiryTime);
}