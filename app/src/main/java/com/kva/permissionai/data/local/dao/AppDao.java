package com.kva.permissionai.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.kva.permissionai.data.local.entities.AppEntity;
import java.util.List;

@Dao
public interface AppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertApp(AppEntity app);

    @Query("SELECT * FROM apps ORDER BY appName ASC")
    LiveData<List<AppEntity>> getAllApps();

    @Query("SELECT * FROM apps WHERE packageName = :packageName LIMIT 1")
    AppEntity getAppByPackage(String packageName);

    @Query("SELECT COUNT(*) FROM apps")
    int getAppCount();
    
    @Query("SELECT * FROM apps WHERE riskLevel = 'High' OR riskLevel = 'Critical' ORDER BY id DESC")
    LiveData<List<AppEntity>> getRiskyApps();

    @Query("DELETE FROM apps")
    void deleteAllApps();

    @Query("DELETE FROM apps WHERE packageName = :packageName")
    void deleteAppByPackage(String packageName);
}