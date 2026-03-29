package com.kva.permissionai.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import com.kva.permissionai.data.local.entities.PermissionEntity;
import java.util.ArrayList;
import java.util.List;

public class PermissionUtils {
    public static List<PermissionEntity> getPermissionsForPackage(Context context, String packageName) {
        List<PermissionEntity> permissions = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            if (packageInfo.requestedPermissions != null) {
                for (int i = 0; i < packageInfo.requestedPermissions.length; i++) {
                    String p = packageInfo.requestedPermissions[i];
                    
                    // Check if this permission is actually granted/allowed
                    int flags = packageInfo.requestedPermissionsFlags[i];
                    if ((flags & PackageInfo.REQUESTED_PERMISSION_GRANTED) == 0) {
                        continue; // Skip if not allowed
                    }

                    PermissionEntity entity = new PermissionEntity();
                    entity.packageName = packageName;
                    entity.permissionName = p;
                    try {
                        PermissionInfo info = pm.getPermissionInfo(p, 0);
                        entity.isDangerous = (info.protectionLevel & PermissionInfo.PROTECTION_DANGEROUS) != 0;
                        entity.description = info.loadDescription(pm) != null ? info.loadDescription(pm).toString() : "No description";
                    } catch (PackageManager.NameNotFoundException e) {
                        entity.isDangerous = false;
                        entity.description = "Custom or unknown permission";
                    }
                    permissions.add(entity);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return permissions;
    }
}