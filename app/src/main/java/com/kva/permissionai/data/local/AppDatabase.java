package com.kva.permissionai.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.kva.permissionai.data.local.dao.AppDao;
import com.kva.permissionai.data.local.dao.PermissionDao;
import com.kva.permissionai.data.local.dao.AiCacheDao;
import com.kva.permissionai.data.local.entities.AppEntity;
import com.kva.permissionai.data.local.entities.PermissionEntity;
import com.kva.permissionai.data.local.entities.AiCacheEntity;

@Database(entities = {AppEntity.class, PermissionEntity.class, AiCacheEntity.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract AppDao appDao();
    public abstract PermissionDao permissionDao();
    public abstract AiCacheDao aiCacheDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "permission_ai_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}