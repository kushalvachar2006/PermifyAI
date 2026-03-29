package com.kva.permissionai.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "apps")
public class AppEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String packageName;
    public String appName;
    public String riskLevel; // Low, Medium, High, Critical
    public String riskExplanation;
    public long lastScanned;
    public boolean isUserConsented; // New field to track user manual override
}