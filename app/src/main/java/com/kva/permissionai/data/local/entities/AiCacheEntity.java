package com.kva.permissionai.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ai_cache")
public class AiCacheEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String queryKey;
    public String aiResponse;
    public long timestamp;
}