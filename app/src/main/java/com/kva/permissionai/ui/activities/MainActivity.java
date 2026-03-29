package com.kva.permissionai.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.kva.permissionai.data.local.AppDatabase;
import com.kva.permissionai.databinding.ActivityMainBinding;
import com.kva.permissionai.ui.adapters.AppAdapter;
import com.kva.permissionai.utils.Constants;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private AppAdapter adapter;
    private AppDatabase db;
    private boolean showRiskyOnly = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = AppDatabase.getInstance(this);
        showRiskyOnly = getIntent().getBooleanExtra(Constants.EXTRA_SHOW_RISKY_ONLY, false);
        
        if (showRiskyOnly) {
            binding.tvTitle.setText("Risky Apps");
        } else {
            binding.tvTitle.setText("All Apps");
        }

        setupRecyclerView();
        observeApps();
        setupSearch();

        binding.ivBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new AppAdapter();
        binding.rvApps.setLayoutManager(new LinearLayoutManager(this));
        binding.rvApps.setAdapter(adapter);

        adapter.setOnAppClickListener(app -> {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(Constants.EXTRA_PACKAGE_NAME, app.packageName);
            startActivity(intent);
        });
    }

    private void observeApps() {
        if (showRiskyOnly) {
            db.appDao().getRiskyApps().observe(this, apps -> {
                if (apps != null) {
                    adapter.setApps(apps);
                }
            });
        } else {
            db.appDao().getAllApps().observe(this, apps -> {
                if (apps != null) {
                    adapter.setApps(apps);
                }
            });
        }
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filter(String query) {
        if (showRiskyOnly) {
            db.appDao().getRiskyApps().observe(this, apps -> {
                if (apps != null) {
                    if (query.isEmpty()) {
                        adapter.setApps(apps);
                    } else {
                        adapter.setApps(apps.stream()
                                .filter(app -> app.appName.toLowerCase().contains(query.toLowerCase()))
                                .collect(Collectors.toList()));
                    }
                }
            });
        } else {
            db.appDao().getAllApps().observe(this, apps -> {
                if (apps != null) {
                    if (query.isEmpty()) {
                        adapter.setApps(apps);
                    } else {
                        adapter.setApps(apps.stream()
                                .filter(app -> app.appName.toLowerCase().contains(query.toLowerCase()))
                                .collect(Collectors.toList()));
                    }
                }
            });
        }
    }
}