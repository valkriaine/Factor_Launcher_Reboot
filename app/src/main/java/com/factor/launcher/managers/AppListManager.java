package com.factor.launcher.managers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import com.factor.launcher.R;
import com.factor.launcher.database.AppListDatabase;
import com.factor.launcher.databinding.AppListItemBinding;
import com.factor.launcher.model.UserApp;
import com.factor.launcher.util.Constants;

import java.util.ArrayList;
import java.util.List;

import static com.factor.launcher.util.Constants.PACKAGE_NAME;

public class AppListManager
{
    private final ArrayList<UserApp> userApps = new ArrayList<>();
    public final AppListAdapter adapter = new AppListAdapter();
    private final Fragment fragment;

    private final AppListDatabase appListDatabase;

    private final SharedPreferences factorSharedPreferences;
    private SharedPreferences.Editor editor;

    public AppListManager(Fragment fragment)
    {
        this.fragment = fragment;
        appListDatabase = Room.databaseBuilder(fragment.requireContext(), AppListDatabase.class, "app_drawer_list").build();

        factorSharedPreferences = fragment.requireContext().getSharedPreferences(PACKAGE_NAME + "_FIRST_LAUNCH", Context.MODE_PRIVATE);

        loadApps(factorSharedPreferences.getBoolean("saved", false));
    }


    //todo: save all user apps in a separate arraylist after retrieving them for the first time to provide customization to the app drawer

    private void loadApps(Boolean isSaved)
    {
        if (isSaved)
        {
            new Thread(() ->
            {
                try
                {
                    PackageManager packageManager = fragment.requireContext().getPackageManager();

                    Intent i = new Intent(Intent.ACTION_MAIN, null);
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    List<ResolveInfo> availableApps = packageManager.queryIntentActivities(i, 0);
                    for (ResolveInfo r : availableApps)
                    {
                        if (!r.activityInfo.packageName.equals(Constants.PACKAGE_NAME))
                        {
                            UserApp app = appListDatabase.appListDao().findByPackage(r.activityInfo.packageName);
                            app.icon = r.activityInfo.loadIcon(packageManager);
                            userApps.add(app);
                        }
                    }

                    fragment.requireActivity().runOnUiThread(adapter::notifyDataSetChanged);
                }
                catch (Exception ex)
                {
                    Log.e("loadApps", ex.getMessage());
                }
            }).start();
        }
        else
        {
            editor = factorSharedPreferences.edit();
            new Thread(() ->
            {
                try
                {
                    PackageManager packageManager = fragment.requireContext().getPackageManager();

                    Intent i = new Intent(Intent.ACTION_MAIN, null);
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    List<ResolveInfo> availableApps = packageManager.queryIntentActivities(i, 0);
                    for (ResolveInfo r : availableApps)
                    {
                        if (!r.activityInfo.packageName.equals(Constants.PACKAGE_NAME))
                        {
                            UserApp app = new UserApp();
                            app.setLabelOld((String) r.loadLabel(packageManager));
                            app.setLabelNew(app.getLabelOld());
                            app.setName(r.activityInfo.packageName);
                            app.icon = r.activityInfo.loadIcon(packageManager);
                            userApps.add(app);
                            appListDatabase.appListDao().insert(app);
                        }
                    }

                    fragment.requireActivity().runOnUiThread(adapter::notifyDataSetChanged);
                    editor.putBoolean("saved", true);
                    editor.apply();

                }
                catch (Exception ex)
                {
                    Toast.makeText(fragment.getContext(), "error loading apps", Toast.LENGTH_LONG).show();
                    Log.e("loadApps", ex.getMessage());
                }
            }).start();
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
                fragment.registerForContextMenu(itemView);
                binding = DataBindingUtil.bind(itemView);
            }

            public void bindApp(UserApp app)
            {
                binding.setUserApp(app);

            }
        }
    }
}
