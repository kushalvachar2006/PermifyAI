package com.kva.permissionai.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "permissions")
public class PermissionEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String packageName;
    public String permissionName;
    public String description;
    public boolean isDangerous;
}