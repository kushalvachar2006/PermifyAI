package com.kva.permissionai.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.kva.permissionai.data.local.entities.PermissionEntity;
import java.util.List;

@Dao
public interface PermissionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPermissions(List<PermissionEntity> permissions);

    @Query("SELECT * FROM permissions WHERE packageName = :packageName")
    List<PermissionEntity> getPermissionsForApp(String packageName);

    @Query("DELETE FROM permissions WHERE packageName = :packageName")
    void deletePermissionsForApp(String packageName);
}