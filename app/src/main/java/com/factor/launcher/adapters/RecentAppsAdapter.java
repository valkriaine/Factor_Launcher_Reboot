package com.factor.launcher.adapters;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.factor.launcher.R;
import com.factor.launcher.databinding.RecentAppsItemBinding;
import com.factor.launcher.models.UserApp;

import java.util.LinkedList;

public class RecentAppsAdapter extends RecyclerView.Adapter<RecentAppsAdapter.RecentAppViewHolder>
{

    private final LinkedList<UserApp> apps;

    public RecentAppsAdapter(LinkedList<UserApp> apps)
    {
        this.apps = apps;
    }

    @NonNull
    @Override
    public RecentAppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recent_apps_item, parent, false);
        return new RecentAppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentAppViewHolder holder, int position)
    {
        holder.bind(apps.get(position));
        if (holder.binding != null)
        {
            holder.binding.recentIcon.setOnLongClickListener(v ->
            {
                UserApp app = holder.binding.getApp();
                int i = apps.indexOf(app);
                apps.remove(app);
                notifyItemRemoved(i);
                return false;
            });
        }
    }

    @Override
    public int getItemCount()
    {
        return apps.size();
    }

    static class RecentAppViewHolder extends RecyclerView.ViewHolder
    {
        private final RecentAppsItemBinding binding;

        public RecentAppViewHolder(@NonNull View itemView)
        {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }

        public void bind(UserApp app)
        {
            binding.setApp(app);
            binding.recentIcon.setImageDrawable(app.getIcon());

            binding.recentIcon.setOnClickListener(v ->
            {
                Intent intent = itemView.getContext().getPackageManager().getLaunchIntentForPackage(app.getPackageName());
                if (intent != null)
                {
                    binding.recentIcon.getContext().startActivity(intent,
                            ActivityOptionsCompat.makeClipRevealAnimation(binding.recentIcon, 0,0,binding.recentIcon.getWidth(), binding.recentIcon.getHeight()).toBundle());
                }
            });
        }
    }
}
