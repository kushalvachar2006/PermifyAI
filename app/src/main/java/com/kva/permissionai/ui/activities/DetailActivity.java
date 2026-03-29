package com.kva.permissionai.ui.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.kva.permissionai.R;
import com.kva.permissionai.data.local.AppDatabase;
import com.kva.permissionai.data.local.entities.AiCacheEntity;
import com.kva.permissionai.data.local.entities.AppEntity;
import com.kva.permissionai.data.local.entities.PermissionEntity;
import com.kva.permissionai.databinding.ActivityDetailBinding;
import com.kva.permissionai.ui.adapters.PermissionAdapter;
import com.kva.permissionai.utils.Constants;
import java.util.List;
import java.util.concurrent.Executors;

public class DetailActivity extends AppCompatActivity {
    private static final int UNINSTALL_REQUEST_CODE = 1001;
    private ActivityDetailBinding binding;
    private AppDatabase db;
    private PermissionAdapter adapter;
    private AppEntity currentApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = AppDatabase.getInstance(this);
        
        setupRecyclerView();
        
        String packageName = getIntent().getStringExtra(Constants.EXTRA_PACKAGE_NAME);
        if (packageName != null) {
            loadAppData(packageName);
        }

        binding.btnUninstall.setOnClickListener(v -> uninstallApp());

        binding.btnTrustApp.setOnClickListener(v -> handleTrustApp());
    }

    private void setupRecyclerView() {
        adapter = new PermissionAdapter();
        binding.rvPermissions.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPermissions.setAdapter(adapter);
    }

    private void loadAppData(String packageName) {
        Executors.newSingleThreadExecutor().execute(() -> {
            currentApp = db.appDao().getAppByPackage(packageName);
            List<PermissionEntity> permissions = db.permissionDao().getPermissionsForApp(packageName);
            AiCacheEntity aiCache = null;
            if (currentApp != null) {
                aiCache = db.aiCacheDao().getCachedResponse(currentApp.appName);
            }

            final AiCacheEntity finalAiCache = aiCache;
            runOnUiThread(() -> {
                if (currentApp != null) {
                    updateUI(currentApp, permissions, finalAiCache);
                }
            });
        });
    }

    private void updateUI(AppEntity app, List<PermissionEntity> permissions, AiCacheEntity aiCache) {
        binding.tvAppNameDetail.setText(app.appName);
        binding.tvRiskBadgeDetail.setText(app.riskLevel.toUpperCase() + " RISK");
        binding.tvRiskReason.setText(app.riskExplanation);
        
        // Improved display logic for AI Insight
        if (aiCache != null && aiCache.aiResponse != null && !aiCache.aiResponse.trim().isEmpty()) {
            String insightText = "As per GEMINI, " + app.appName + " requests the following permissions:- " + aiCache.aiResponse.trim();
            binding.tvAiInsight.setText(insightText);
        } else {
            // If cache is missing, use the explanation which contains AI analysis keywords
            if (app.riskExplanation != null && app.riskExplanation.startsWith("AI Analysis:")) {
                 binding.tvAiInsight.setText("As per GEMINI, " + app.appName + " is being analyzed. " + app.riskExplanation);
            } else {
                 binding.tvAiInsight.setText("AI analysis for '" + app.appName + "' is currently being updated. Please run a new scan to refresh insights.");
            }
        }

        try {
            PackageManager pm = getPackageManager();
            Drawable icon = pm.getApplicationIcon(app.packageName);
            binding.ivAppIconDetail.setImageDrawable(icon);
        } catch (PackageManager.NameNotFoundException e) {
            binding.ivAppIconDetail.setImageResource(android.R.drawable.sym_def_app_icon);
        }

        int badgeBg;
        boolean isRisky = app.riskLevel.equals("High") || app.riskLevel.equals("Critical");
        
        switch (app.riskLevel) {
            case "Low": 
                badgeBg = R.drawable.bg_badge_low; 
                break;
            case "Medium": 
                badgeBg = R.drawable.bg_badge_medium; 
                break;
            default: 
                badgeBg = R.drawable.bg_badge_high; 
                break;
        }
        
        binding.tvRiskBadgeDetail.setBackgroundResource(badgeBg);
        binding.cardRecommendedAction.setVisibility(isRisky ? View.VISIBLE : View.GONE);
        binding.cardConsent.setVisibility((isRisky && !app.isUserConsented) ? View.VISIBLE : View.GONE);

        adapter.setPermissions(permissions);
    }

    private void handleTrustApp() {
        if (currentApp == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            currentApp.isUserConsented = true;
            currentApp.riskLevel = "Low";
            currentApp.riskExplanation = "User Choice: You have acknowledged and trusted these permissions.";
            
            db.appDao().insertApp(currentApp);
            AiCacheEntity aiCache = db.aiCacheDao().getCachedResponse(currentApp.appName);

            runOnUiThread(() -> {
                updateUI(currentApp, adapter.getRawPermissions(), aiCache);
                Toast.makeText(this, currentApp.appName + " marked as trusted.", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void uninstallApp() {
        if (currentApp == null) return;
        try {
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(Uri.parse("package:" + currentApp.packageName));
            startActivityForResult(intent, UNINSTALL_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(this, "Error starting uninstallation: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UNINSTALL_REQUEST_CODE) {
            checkIfAppIsUninstalled();
        }
    }

    private void checkIfAppIsUninstalled() {
        if (currentApp == null) return;
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(currentApp.packageName, 0);
            // App still exists
        } catch (PackageManager.NameNotFoundException e) {
            // App uninstalled successfully
            Executors.newSingleThreadExecutor().execute(() -> {
                db.appDao().deleteAppByPackage(currentApp.packageName);
                db.permissionDao().deletePermissionsForApp(currentApp.packageName);
                
                runOnUiThread(() -> {
                    Toast.makeText(this, "App uninstalled and removed from list.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, DashboardActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                });
            });
        }
    }
}