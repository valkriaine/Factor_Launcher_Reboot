package com.factor.launcher.adapters;

import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.factor.launcher.receivers.AppActionReceiver;
import com.factor.launcher.util.Constants;
import com.factor.launcher.view_models.AppListManager;

import java.util.LinkedList;

public class RecentAppsAdapter extends RecyclerView.Adapter<RecentAppsAdapter.RecentAppViewHolder>
{
    private final LinkedList<String> apps;

    private final AppListManager appListManager;

    public RecentAppsAdapter(LinkedList<String> apps, AppListManager appListManager)
    {
        this.apps = apps;
        this.appListManager = appListManager;
    }

    public void addRecentBroadcast()
    {
        Intent intent = new Intent(appListManager.getActivity(), AppActionReceiver.class);
        intent.setAction(Constants.BROADCAST_ACTION_ADD);
        appListManager.getActivity().sendBroadcast(intent);
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
        holder.bind(apps.get(position), appListManager);
        if (holder.binding != null)
        {
            holder.binding.recentIcon.setOnLongClickListener(v ->
            {
                String packageName = holder.binding.getPackageName();
                int i = apps.indexOf(packageName);
                apps.remove(packageName);
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

        public void bind(String name, AppListManager appListManager)
        {
            binding.setPackageName(name);
            try
            {
                binding.recentIcon.setImageDrawable(appListManager.packageManager.getApplicationIcon(name));
            }
            catch (PackageManager.NameNotFoundException ignored){}

            binding.recentIcon.setOnClickListener(v ->
            {
                Intent intent = itemView.getContext().getPackageManager().getLaunchIntentForPackage(name);
                if (intent != null)
                {
                    UserApp app = new UserApp();
                    app.setPackageName(name);
                    appListManager.addToRecent(app);
                    binding.recentIcon.getContext().startActivity(intent,
                            ActivityOptionsCompat.makeClipRevealAnimation(binding.recentIcon, 0,0,binding.recentIcon.getWidth(), binding.recentIcon.getHeight()).toBundle());
                }
            });
        }
    }
}
