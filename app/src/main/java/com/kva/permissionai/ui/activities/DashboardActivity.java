package com.kva.permissionai.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kva.permissionai.R;
import com.kva.permissionai.data.local.AppDatabase;
import com.kva.permissionai.databinding.ActivityDashboardBinding;
import com.kva.permissionai.ui.adapters.AppAdapter;
import com.kva.permissionai.utils.Constants;
import com.kva.permissionai.utils.NetworkUtils;
import com.kva.permissionai.workers.ScanWorker;
import java.util.concurrent.Executors;

public class DashboardActivity extends AppCompatActivity {
    private ActivityDashboardBinding binding;
    private AppDatabase db;
    private AppAdapter adapter;
    private static boolean isFreshStart = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = AppDatabase.getInstance(this);
        
        if (isFreshStart) {
            Executors.newSingleThreadExecutor().execute(() -> {
                db.appDao().deleteAllApps();
            });
            isFreshStart = false;
        }

        setupRecyclerView();
        observeData();

        binding.btnScanNow.setOnClickListener(v -> startScan());

        binding.cardRiskyApps.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(Constants.EXTRA_SHOW_RISKY_ONLY, true);
            startActivity(intent);
        });
        
        binding.cardTotalApps.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(Constants.EXTRA_SHOW_RISKY_ONLY, false);
            startActivity(intent);
        });

        binding.ivInfo.setOnClickListener(v -> showAppInfo());

        binding.tvScoreValue.setText("0");
        binding.pbScore.setProgress(0);
        binding.tvScanStatus.setText(R.string.scan_idle);
        binding.tvScoreFeedback.setText("");
    }

    private void setupRecyclerView() {
        adapter = new AppAdapter();
        binding.rvTopRisky.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTopRisky.setAdapter(adapter);

        adapter.setOnAppClickListener(app -> {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(Constants.EXTRA_PACKAGE_NAME, app.packageName);
            startActivity(intent);
        });
    }

    private void observeData() {
        db.appDao().getAllApps().observe(this, apps -> {
            if (apps != null && !apps.isEmpty()) {
                binding.tvTotalApps.setText(String.valueOf(apps.size()));
                binding.tvGuide.setVisibility(View.GONE);
            } else {
                binding.tvTotalApps.setText("0");
                binding.tvGuide.setVisibility(View.VISIBLE);
                binding.tvScoreValue.setText("0");
                binding.pbScore.setProgress(0);
                binding.tvScoreFeedback.setText("");
            }
        });

        db.appDao().getRiskyApps().observe(this, riskyApps -> {
            if (riskyApps != null && !riskyApps.isEmpty()) {
                binding.tvRiskyApps.setText(String.valueOf(riskyApps.size()));
                adapter.setApps(riskyApps);
                updateScore(riskyApps.size());
            } else {
                binding.tvRiskyApps.setText("0");
                adapter.setApps(null);
                updateScore(0);
            }
        });
    }

    private void updateScore(int riskyCount) {
        int totalApps = 0;
        try {
            totalApps = Integer.parseInt(binding.tvTotalApps.getText().toString());
        } catch (NumberFormatException ignored) {}

        if (totalApps == 0) {
            binding.tvScoreValue.setText("0");
            binding.pbScore.setProgress(0);
            binding.tvScoreFeedback.setText("");
            return;
        }

        int score = 100 - (riskyCount * 100 / totalApps);
        if (score < 0) score = 0;

        binding.tvScoreValue.setText(String.valueOf(score));
        binding.pbScore.setProgress(score);
        updateFeedback(score);
    }

    private void updateFeedback(int score) {
        String feedback;
        if (score >= 90) {
            feedback = "Your privacy is well protected. Great job!";
        } else if (score >= 70) {
            feedback = "Most apps are safe, but keep an eye on some permissions.";
        } else if (score >= 40) {
            feedback = "Warning: Several apps have unusual permissions. Review them soon.";
        } else {
            feedback = "Critical: High risk detected! Secure your device by reviewing flagged apps.";
        }
        binding.tvScoreFeedback.setText(feedback);
    }

    private void startScan() {
        if (!NetworkUtils.isInternetAvailable(this)) {
            Toast.makeText(this, "Internet connection required for AI scan", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.tvTotalApps.setText("0");
        binding.tvRiskyApps.setText("0");
        binding.tvScoreValue.setText("0");
        binding.pbScore.setProgress(0);
        binding.tvScoreFeedback.setText("Initializing refresh...");
        binding.tvScanStatus.setText(R.string.scan_running);
        binding.btnScanNow.setEnabled(false);
        adapter.setApps(null);

        Executors.newSingleThreadExecutor().execute(() -> {
            db.appDao().deleteAllApps();
            
            runOnUiThread(() -> {
                OneTimeWorkRequest scanRequest = new OneTimeWorkRequest.Builder(ScanWorker.class).build();
                WorkManager.getInstance(this).enqueue(scanRequest);
                
                binding.tvScoreFeedback.setText("AI is analyzing your app permissions...");
                
                WorkManager.getInstance(this).getWorkInfoByIdLiveData(scanRequest.getId())
                        .observe(this, workInfo -> {
                            if (workInfo != null) {
                                if (workInfo.getState().isFinished()) {
                                    binding.tvScanStatus.setText(R.string.scan_finished);
                                    binding.btnScanNow.setEnabled(true);
                                    binding.tvGuide.setVisibility(View.GONE);
                                } else if (workInfo.getState() == WorkInfo.State.RUNNING) {
                                    binding.tvScanStatus.setText(R.string.scan_running);
                                }
                            }
                        });
            });
        });
    }

    private void showAppInfo() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_about, null);
        new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setPositiveButton("Got it", null)
                .show();
    }
}