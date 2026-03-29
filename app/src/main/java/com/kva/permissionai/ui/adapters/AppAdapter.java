package com.kva.permissionai.ui.adapters;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.kva.permissionai.R;
import com.kva.permissionai.data.local.entities.AppEntity;
import com.kva.permissionai.databinding.ItemAppBinding;
import java.util.ArrayList;
import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppViewHolder> {

    private List<AppEntity> apps = new ArrayList<>();
    private OnAppClickListener listener;

    public interface OnAppClickListener {
        void onAppClick(AppEntity app);
    }

    public void setOnAppClickListener(OnAppClickListener listener) {
        this.listener = listener;
    }

    public void setApps(List<AppEntity> apps) {
        if (apps == null) {
            this.apps = new ArrayList<>();
        } else {
            this.apps = apps;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAppBinding binding = ItemAppBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AppViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppEntity app = apps.get(position);
        holder.binding.tvAppName.setText(app.appName);
        holder.binding.tvRiskSummary.setText(app.riskExplanation != null ? app.riskExplanation : "No risk detected");
        holder.binding.tvRiskBadge.setText(app.riskLevel.toUpperCase());

        try {
            PackageManager pm = holder.itemView.getContext().getPackageManager();
            Drawable icon = pm.getApplicationIcon(app.packageName);
            holder.binding.ivAppIcon.setImageDrawable(icon);
        } catch (PackageManager.NameNotFoundException e) {
            holder.binding.ivAppIcon.setImageResource(android.R.drawable.sym_def_app_icon);
        }

        int badgeBg;
        switch (app.riskLevel) {
            case "Low": badgeBg = R.drawable.bg_badge_low; break;
            case "Medium": badgeBg = R.drawable.bg_badge_medium; break;
            case "High":
            case "Critical":
            default: badgeBg = R.drawable.bg_badge_high; break;
        }
        holder.binding.tvRiskBadge.setBackgroundResource(badgeBg);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onAppClick(app);
        });
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        ItemAppBinding binding;
        AppViewHolder(ItemAppBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}