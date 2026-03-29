package com.kva.permissionai.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.kva.permissionai.R;
import com.kva.permissionai.data.local.entities.PermissionEntity;
import com.kva.permissionai.databinding.ItemPermissionBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PermissionAdapter extends RecyclerView.Adapter<PermissionAdapter.PermissionViewHolder> {

    private List<PermissionEntity> permissions = new ArrayList<>();

    public void setPermissions(List<PermissionEntity> permissions) {
        // Only show "Main" (Dangerous) permissions in the list
        if (permissions != null) {
            this.permissions = permissions.stream()
                    .filter(p -> p.isDangerous)
                    .collect(Collectors.toList());
        } else {
            this.permissions = new ArrayList<>();
        }
        notifyDataSetChanged();
    }

    public List<PermissionEntity> getRawPermissions() {
        return permissions;
    }

    @NonNull
    @Override
    public PermissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPermissionBinding binding = ItemPermissionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PermissionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PermissionViewHolder holder, int position) {
        PermissionEntity permission = permissions.get(position);
        String name = permission.permissionName.substring(permission.permissionName.lastIndexOf('.') + 1);
        holder.binding.tvPermissionName.setText(name);
        holder.binding.tvPermissionDesc.setText(permission.description);
        
        // Since we only show dangerous ones here, we can style them accordingly
        holder.binding.ivPermissionIcon.setColorFilter(holder.itemView.getContext().getColor(R.color.risk_high));
        holder.binding.tvPermissionName.setTextColor(holder.itemView.getContext().getColor(R.color.risk_high));
    }

    @Override
    public int getItemCount() {
        return permissions.size();
    }

    static class PermissionViewHolder extends RecyclerView.ViewHolder {
        ItemPermissionBinding binding;
        PermissionViewHolder(ItemPermissionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}