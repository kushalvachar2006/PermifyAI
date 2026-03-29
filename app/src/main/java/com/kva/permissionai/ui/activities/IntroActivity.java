package com.kva.permissionai.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.appcompat.app.AppCompatActivity;
import com.kva.permissionai.R;
import com.kva.permissionai.databinding.ActivityIntroBinding;

public class IntroActivity extends AppCompatActivity {
    private ActivityIntroBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIntroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Load animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up_bounce);

        // Apply animations
        binding.ivLogo.startAnimation(fadeIn);
        binding.tvAppName.startAnimation(slideUp);
        binding.tvTagline.startAnimation(fadeIn);

        // Transition to Dashboard
        new Handler().postDelayed(() -> {
            startActivity(new Intent(IntroActivity.this, DashboardActivity.class));
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, 3000);
    }
}