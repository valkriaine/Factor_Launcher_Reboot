package com.factor.launcher.managers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.factor.launcher.R;
import com.factor.launcher.databinding.AppListItemBinding;
import com.factor.launcher.model.UserApp;
import com.factor.launcher.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class AppListManager
{
    private final ArrayList<UserApp> userApps = new ArrayList<>();
    public final AppListAdapter adapter = new AppListAdapter();

    private final Context context;

    public AppListManager(Context context)
    {
        this.context = context;
        loadApps();
    }


    private void loadApps()
    {
        try {
            PackageManager packageManager = context.getPackageManager();

            Intent i = new Intent(Intent.ACTION_MAIN, null);
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> availableApps = packageManager.queryIntentActivities(i, 0);
            for (ResolveInfo r : availableApps)
            {
                if (!r.activityInfo.packageName.equals(Constants.PACKAGE_NAME))
                {
                    UserApp app = new UserApp();
                    app.label = r.loadLabel(packageManager);
                    app.name = r.activityInfo.packageName;
                    app.icon = r.activityInfo.loadIcon(packageManager);
                    userApps.add(app);
                }
            }
            adapter.notifyDataSetChanged();
        }
        catch (Exception ex)
        {
            Toast.makeText(context, ex.getMessage() + " loadApps", Toast.LENGTH_LONG).show();
            Log.e("Error loadApps", ex.getMessage() + " loadApps");
        }
    }



    class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppListViewHolder>
    {
        @NonNull
        @Override
        public AppListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_list_item, parent, false);
            return new AppListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AppListViewHolder holder, int position)
        {
            holder.bindApp(userApps.get(position));
        }

        @Override
        public int getItemCount()
        {
            return userApps.size();
        }


        class AppListViewHolder extends RecyclerView.ViewHolder
        {
            private final AppListItemBinding binding;
            public AppListViewHolder(@NonNull View itemView)
            {
                super(itemView);
                binding = DataBindingUtil.bind(itemView);
            }

            public void bindApp(UserApp app)
            {
                binding.setUserApp(app);
            }
        }

    }
}
