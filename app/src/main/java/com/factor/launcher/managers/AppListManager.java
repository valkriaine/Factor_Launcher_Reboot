package com.factor.launcher.managers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import com.factor.launcher.R;
import com.factor.launcher.database.AppListDatabase;
import com.factor.launcher.databinding.AppListItemBinding;
import com.factor.launcher.model.UserApp;
import com.factor.launcher.util.Constants;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.factor.launcher.util.Constants.PACKAGE_NAME;

public class AppListManager
{
    private static final String TAG = "AppListManager";

    private final ArrayList<UserApp> userApps = new ArrayList<>();
    public final AppListAdapter adapter = new AppListAdapter();
    private final Activity activity;

    private final AppListDatabase appListDatabase;

    private final SharedPreferences factorSharedPreferences;
    private SharedPreferences.Editor editor;


    private FactorManager factorManager;

    //constructor
    public AppListManager(Activity activity)
    {
        this.activity = activity;
        appListDatabase = Room.databaseBuilder(activity, AppListDatabase.class, "app_drawer_list").build();
        this.factorManager = new FactorManager(activity);
        factorSharedPreferences = activity.getSharedPreferences(PACKAGE_NAME + "_FIRST_LAUNCH", Context.MODE_PRIVATE);
        loadApps(factorSharedPreferences.getBoolean("saved", false));
    }

    //compare app label (new)
    private final Comparator<UserApp> first_letter = new Comparator<UserApp>()
    {
        private final Collator sCollator = Collator.getInstance();
        @Override
        public int compare(UserApp app1, UserApp app2)
        {
            return sCollator.compare(app1.getLabelNew(), app2.getLabelNew());
        }
    };

    //return activity
    public Activity getActivity()
    {
        return this.activity;
    }

    //load app drawer list
    private void loadApps(Boolean isSaved)
    {
        if (isSaved)
        {
            new Thread(() ->
            {
                try
                {
                    PackageManager packageManager = activity.getPackageManager();
                    Intent i = new Intent(Intent.ACTION_MAIN, null);
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    List<ResolveInfo> availableApps = packageManager.queryIntentActivities(i, 0);
                    for (ResolveInfo r : availableApps)
                    {
                        if (!r.activityInfo.packageName.equals(Constants.PACKAGE_NAME))
                        {
                            UserApp app = appListDatabase.appListDao().findByPackage(r.activityInfo.packageName);
                            //noinspection ConstantConditions
                            if (app == null) //package name does not exist in database
                            {
                                app = new UserApp();
                                app.setLabelOld((String) r.loadLabel(packageManager));
                                app.setLabelNew(app.getLabelOld());
                                app.setPackageName(r.activityInfo.packageName);
                                app.icon = r.activityInfo.loadIcon(packageManager);
                                userApps.add(app);
                                appListDatabase.appListDao().insert(app);
                            }
                            else
                            {
                                if (doesPackageExist(app) && packageManager.getApplicationInfo(app.getPackageName(), 0).enabled)
                                {
                                    app.icon = r.activityInfo.loadIcon(packageManager);
                                    userApps.add(app);
                                    userApps.sort(first_letter);
                                }
                                else
                                {
                                    appListDatabase.appListDao().delete(app);
                                }
                            }
                        }
                    }
                    activity.runOnUiThread(adapter::notifyDataSetChanged);
                }
                catch (Exception ex)
                {
                    Log.e(TAG, ex.getMessage());
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
                    PackageManager packageManager = activity.getPackageManager();

                    Intent i = new Intent(Intent.ACTION_MAIN, null);
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    List<ResolveInfo> availableApps = packageManager.queryIntentActivities(i, 0);
                    for (ResolveInfo r : availableApps)
                    {
                        if (!r.activityInfo.packageName.equals(Constants.PACKAGE_NAME) && packageManager.getApplicationInfo(r.activityInfo.packageName, 0).enabled)
                        {
                            UserApp app = new UserApp();
                            app.setLabelOld((String) r.loadLabel(packageManager));
                            app.setLabelNew(app.getLabelOld());
                            app.setPackageName(r.activityInfo.packageName);
                            app.icon = r.activityInfo.loadIcon(packageManager);
                            userApps.add(app);
                        }
                    }
                    userApps.sort(first_letter);
                    appListDatabase.appListDao().insertAll(userApps);
                    activity.runOnUiThread(adapter::notifyDataSetChanged);
                    editor.putBoolean("saved", true);
                    editor.apply();

                }
                catch (Exception ex)
                {
                    Log.d(TAG, ex.getMessage());
                }
            }).start();
        }




    }

    //pin & unpin
    private boolean changePin(UserApp userApp)
    {
        userApp.changePinnedState();
        if (userApp.isPinned())
            factorManager.addToHome(userApp);

        new Thread(() ->
        {
            appListDatabase.appListDao().updateAppInfo(userApp);
            activity.runOnUiThread(() -> adapter.notifyItemChanged(userApps.indexOf(userApp)));
        }).start();

        return userApps.contains(userApp);
    }

    //check if package exists
    private boolean doesPackageExist(UserApp a)
    {
        boolean result = false;
        PackageManager packageManager = activity.getPackageManager();
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> availableApps = packageManager.queryIntentActivities(i, 0);
        for (ResolveInfo r : availableApps)
        {
            if (!r.activityInfo.packageName.equals(Constants.PACKAGE_NAME))
            {
                if (r.activityInfo.packageName.equals(a.getPackageName()))
                {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    //remove app from the list only if package no longer exists
    public void removeApp(UserApp app)
    {
        if (!doesPackageExist(app))
        {
            int position = userApps.indexOf(app);
            userApps.remove(app);
            new Thread(() ->
            {

                appListDatabase.appListDao().delete(app);
                activity.runOnUiThread(() -> adapter.notifyItemRemoved(position));
            }).start();
            factorManager.remove(app);
        }

        //todo: remove from home screen if pinned
    }

    //add app after receiving PACKAGE_ADDED broadcast
    public void addApp(UserApp app)
    {
        if (doesPackageExist(app))
        {
            PackageManager packageManager = activity.getPackageManager();
            new Thread(() ->
            {
                try
                {
                    ApplicationInfo info = packageManager.getApplicationInfo(app.getPackageName(), 0);
                    app.setIcon(packageManager.getApplicationIcon(app.getPackageName()));
                    app.setLabelOld((String) packageManager.getApplicationLabel(info));
                    app.setLabelNew(app.getLabelOld());
                    appListDatabase.appListDao().insert(app);
                    userApps.add(app);
                    userApps.sort(first_letter);
                    activity.runOnUiThread(() -> adapter.notifyItemInserted(userApps.indexOf(app)));
                }
                catch (PackageManager.NameNotFoundException e)
                {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    //called when receiving PACKAGE_ADDED broadcast with EXTRA_REPLACING set to true
    public void updateApp(UserApp app)
    {
        PackageManager packageManager = activity.getPackageManager();
        try {
            if (doesPackageExist(app) && packageManager.getApplicationInfo(app.getPackageName(), 0).enabled)
            {
                if (userApps.contains(app))
                {
                    int position = userApps.indexOf(app);
                    new Thread(() ->
                    {
                        try
                        {
                            ApplicationInfo info = packageManager.getApplicationInfo(app.getPackageName(), 0);
                            userApps.get(position).setIcon(packageManager.getApplicationIcon(app.getPackageName()));
                            userApps.get(position).setLabelOld((String) packageManager.getApplicationLabel(info));
                            appListDatabase.appListDao().updateAppInfo(userApps.get(position));
                            userApps.sort(first_letter);
                            activity.runOnUiThread(() -> adapter.notifyItemChanged(position));

                            if (app.isPinned())
                                factorManager.updateFactor(app);
                        }
                        catch (PackageManager.NameNotFoundException e)
                        {
                            e.printStackTrace();
                        }
                    }).start();
                }
                else
                {
                    addApp(app);
                }
            }
            else
            {
                if (userApps.contains(app))
                {
                    removeApp(app);
                }
            }
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public FactorManager getFactorManager()
    {
        return this.factorManager;
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
                activity.registerForContextMenu(itemView);
                itemView.setOnCreateContextMenuListener((menu, v, menuInfo) ->
                {
                    UserApp a = binding.getUserApp();
                    MenuInflater inflater = activity.getMenuInflater();
                    inflater.inflate(R.menu.app_list_item_menu, menu);
                    if (a.isPinned())
                        menu.getItem(0).setEnabled(false);

                    //add to home & remove from home
                    menu.getItem(0).setOnMenuItemClickListener(item -> changePin(binding.getUserApp()));

                    //todo: edit app
                    menu.getItem(1).setOnMenuItemClickListener(item -> changePin(binding.getUserApp()));

                    //info
                    menu.getItem(2).setOnMenuItemClickListener(item ->
                    {
                        activity.startActivity(
                                new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.parse("package:"+binding.getUserApp().getPackageName())));
                        return true;
                    });

                    //uninstall
                    menu.getItem(3).setOnMenuItemClickListener(item ->
                    {
                        activity.startActivity(new Intent(Intent.ACTION_DELETE, Uri.parse("package:"+binding.getUserApp().getPackageName()))
                                .putExtra(Intent.EXTRA_RETURN_RESULT, true));
                        return true;
                    });
                });
            }
        }
    }
}
