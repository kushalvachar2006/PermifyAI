package com.kva.permissionai.workers;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.kva.permissionai.data.local.AppDatabase;
import com.kva.permissionai.data.local.entities.AiCacheEntity;
import com.kva.permissionai.data.local.entities.AppEntity;
import com.kva.permissionai.data.local.entities.PermissionEntity;
import com.kva.permissionai.data.remote.AiService;
import com.kva.permissionai.data.remote.ApiClient;
import com.kva.permissionai.utils.PermissionUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Response;

public class ScanWorker extends Worker {
    public ScanWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        PackageManager pm = context.getPackageManager();
        AppDatabase db = AppDatabase.getInstance(context);
        AiService aiService = ApiClient.getClient().create(AiService.class);

        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo appInfo : apps) {
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) continue;

            String packageName = appInfo.packageName;
            String appName = pm.getApplicationLabel(appInfo).toString();
            List<PermissionEntity> currentPermissions = PermissionUtils.getPermissionsForPackage(context, packageName);
            
            AppEntity existingEntity = db.appDao().getAppByPackage(packageName);
            boolean wasConsented = (existingEntity != null && existingEntity.isUserConsented);

            String aiRecommendedText = getCachedOrAiPermissions(db, aiService, appName);
            
            String riskLevel = "Low";
            StringBuilder explanation = new StringBuilder();
            
            if (aiRecommendedText != null && !aiRecommendedText.isEmpty()) {
                int extraDangerousCount = 0;
                List<String> extraPermissions = new ArrayList<>();
                String aiTextLower = aiRecommendedText.toLowerCase();
                
                for (PermissionEntity p : currentPermissions) {
                    if (p.isDangerous && !isPermissionCovered(p.permissionName, aiTextLower)) {
                        extraDangerousCount++;
                        extraPermissions.add(p.permissionName.substring(p.permissionName.lastIndexOf('.') + 1));
                    }
                }
                
                if (extraDangerousCount > 0) {
                    if (wasConsented) {
                        riskLevel = "Low";
                        explanation.append("User Choice: You have knowingly allowed these permissions. Flagged as Low Risk.");
                    } else {
                        riskLevel = extraDangerousCount > 2 ? "Critical" : "High";
                        explanation.append("AI Analysis: '").append(appName).append("' requests access to ")
                                .append(String.join(", ", extraPermissions))
                                .append(", which is not typically required for this app category.");
                    }
                } else {
                    riskLevel = "Low";
                    explanation.append("AI Analysis: Permissions align with standard requirements for '").append(appName).append("'.");
                }
            } else {
                long dangerousCount = currentPermissions.stream().filter(p -> p.isDangerous).count();
                if (wasConsented) {
                    riskLevel = "Low";
                    explanation.append("User Choice: Permissions acknowledged by you.");
                } else {
                    riskLevel = dangerousCount > 5 ? "High" : (dangerousCount > 0 ? "Medium" : "Low");
                    explanation.append("Risk Analysis: Detected ").append(dangerousCount).append(" sensitive permissions.");
                }
            }

            db.permissionDao().deletePermissionsForApp(packageName);
            db.permissionDao().insertPermissions(currentPermissions);

            AppEntity entity = new AppEntity();
            entity.packageName = packageName;
            entity.appName = appName;
            entity.lastScanned = System.currentTimeMillis();
            entity.riskLevel = riskLevel;
            entity.riskExplanation = explanation.toString();
            entity.isUserConsented = wasConsented;
            
            db.appDao().insertApp(entity);
        }

        return Result.success();
    }

    private boolean isPermissionCovered(String permissionName, String aiText) {
        String fullPerm = permissionName.toLowerCase();
        String shortPerm = fullPerm.substring(fullPerm.lastIndexOf('.') + 1);
        
        // Direct checks
        if (aiText.contains(fullPerm) || aiText.contains(shortPerm)) return true;

        // Keyword mapping for common Android permissions
        Map<String, List<String>> keywordMap = new HashMap<>();
        keywordMap.put("location", Arrays.asList("fine_location", "coarse_location", "background_location"));
        keywordMap.put("contacts", Arrays.asList("read_contacts", "write_contacts", "get_accounts"));
        keywordMap.put("storage", Arrays.asList("read_external_storage", "write_external_storage", "manage_external_storage", "read_media_images", "read_media_video", "read_media_audio"));
        keywordMap.put("photos", Arrays.asList("read_media_images", "read_external_storage"));
        keywordMap.put("videos", Arrays.asList("read_media_video", "read_external_storage"));
        keywordMap.put("microphone", Arrays.asList("record_audio"));
        keywordMap.put("phone", Arrays.asList("call_phone", "read_phone_state", "read_call_log", "write_call_log"));
        keywordMap.put("sms", Arrays.asList("send_sms", "receive_sms", "read_sms", "receive_mms", "receive_wap_push"));
        keywordMap.put("camera", Arrays.asList("camera"));
        keywordMap.put("calendar", Arrays.asList("read_calendar", "write_calendar"));
        keywordMap.put("sensors", Arrays.asList("body_sensors"));
        keywordMap.put("notifications", Arrays.asList("post_notifications"));
        keywordMap.put("nearby", Arrays.asList("bluetooth_scan", "bluetooth_connect", "bluetooth_advertise", "nearby_wifi_devices"));

        for (Map.Entry<String, List<String>> entry : keywordMap.entrySet()) {
            if (aiText.contains(entry.getKey())) {
                for (String perm : entry.getValue()) {
                    if (shortPerm.contains(perm)) return true;
                }
            }
        }
        
        // Fallback for very common words
        if (aiText.contains("location") && shortPerm.contains("location")) return true;
        if (aiText.contains("contacts") && shortPerm.contains("contacts")) return true;
        if (aiText.contains("storage") && shortPerm.contains("storage")) return true;
        if (aiText.contains("audio") && shortPerm.contains("audio")) return true;
        if (aiText.contains("video") && shortPerm.contains("video")) return true;
        if (aiText.contains("image") && shortPerm.contains("image")) return true;

        return false;
    }

    private String getCachedOrAiPermissions(AppDatabase db, AiService service, String appName) {
        AiCacheEntity cached = db.aiCacheDao().getCachedResponse(appName);
        if (cached != null && (System.currentTimeMillis() - cached.timestamp < 1000L * 60 * 60 * 24 * 7)) {
            return cached.aiResponse;
        }

        String promptText = "List the typical and necessary Android permissions for an app named '" + appName + "'. " +
                "Include standard ones like CAMERA, CONTACTS, STORAGE, PHOTOS, VIDEOS, LOCATION, SMS, PHONE, MICROPHONE, NOTIFICATIONS, NEARBY DEVICES, etc. " +
                "Return a simple comma-separated list of keywords. No preamble.";

        AiService.Part part = new AiService.Part(promptText);
        AiService.Content content = new AiService.Content(Collections.singletonList(part));
        AiService.GeminiRequest request = new AiService.GeminiRequest(Collections.singletonList(content));

        try {
            Response<AiService.GeminiResponse> response = service.generateContent(ApiClient.API_KEY, request).execute();
            if (response.isSuccessful() && response.body() != null && !response.body().candidates.isEmpty()) {
                String aiText = response.body().candidates.get(0).content.parts.get(0).text;
                
                AiCacheEntity newCache = new AiCacheEntity();
                newCache.queryKey = appName;
                newCache.aiResponse = aiText;
                newCache.timestamp = System.currentTimeMillis();
                db.aiCacheDao().cacheAiResponse(newCache);
                
                return aiText;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (cached != null) ? cached.aiResponse : "";
    }
}