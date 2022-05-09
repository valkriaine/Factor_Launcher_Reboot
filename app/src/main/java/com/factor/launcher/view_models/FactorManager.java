package com.factor.launcher.view_models;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.factor.launcher.adapters.FactorsAdapter;
import com.factor.launcher.database.FactorsDatabase;
import com.factor.launcher.models.AppSettings;
import com.factor.launcher.models.AppShortcut;
import com.factor.launcher.models.Factor;
import com.factor.launcher.models.UserApp;
import com.factor.launcher.util.Payload;
import com.factor.launcher.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class FactorManager extends ViewModel
{
    private final MutableLiveData<ArrayList<Factor>> factorMutableLiveData = new MutableLiveData<>();

    private final ArrayList<Factor> userFactors = new ArrayList<>();

    private final FactorsDatabase.FactorsDao daoReference;

    private final LauncherApps.ShortcutQuery shortcutQuery;

    public PackageManager packageManager;

    public FactorsAdapter adapter;

    public final LauncherApps launcherApps;

    private AppSettings appSettings;

    private final AppListManager appListManager;

    //constructor
    public FactorManager(Activity activity,
                         ViewGroup background,
                         AppListManager appListManager,
                         PackageManager pm,
                         LauncherApps launcherApps,
                         LauncherApps.ShortcutQuery shortcutQuery,
                         Boolean isLiveWallpaper)
    {
        this.packageManager = pm;
        this.appListManager = appListManager;
        this.shortcutQuery = shortcutQuery;
        this.launcherApps = launcherApps;
        this.appSettings = AppSettingsManager.getInstance(activity.getApplication()).getAppSettings();

        adapter = new FactorsAdapter(this, appSettings, activity, isLiveWallpaper, userFactors, background);
        daoReference = FactorsDatabase.Companion.getInstance(activity.getApplicationContext()).factorsDao();
        loadFactors();

        factorMutableLiveData.setValue(userFactors);
    }

    public MutableLiveData<ArrayList<Factor>> getFactorMutableLiveData()
    {
        return this.factorMutableLiveData;
    }

    //load factors from db
    private void loadFactors()
    {
        new Thread(()->
        {
            userFactors.addAll(daoReference.getAll());
            for (Factor f: userFactors)
            {
                try {
                    if (packageManager.getApplicationInfo(f.getPackageName(), 0).enabled)
                    {
                        Drawable icon = packageManager.getApplicationIcon(f.getPackageName());
                        f.setIcon(icon);
                        Bitmap b = Util.INSTANCE.drawableToBitmap(icon);
                        f.setDominantColor(Util.INSTANCE.getDominantColor(b));
                        f.setDarkMutedColor(Util.INSTANCE.getDarkMutedColor(b));
                        f.setVibrantColor(Util.INSTANCE.getVibrantColor(b));

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
                            f.setShortcuts(getShortcutsFromFactor(f));
                    }

                } catch (PackageManager.NameNotFoundException | NullPointerException e)
                {
                    Log.d("icon", "failed to load icon for " + f.getPackageName() + " " + e.getMessage());
                    daoReference.delete(f);
                }
            }
            adapter.activity.runOnUiThread(adapter::notifyDataSetChanged);
        }).start();
    }

    //add recently used app
    public void addToRecent(UserApp app)
    {
        Log.d("recent", "called");
        if (!app.isHidden())
           appListManager.recentAppsHost.add(app);
    }

    public UserApp findAppByPackage(String name)
    {
        return appListManager.findAppByPackage(name);
    }

    //add new app to home
    public void addToHome(UserApp app)
    {
        Factor factor = app.toFactor();
        userFactors.add(factor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
            factor.setShortcuts(getShortcutsFromFactor(factor));

        factor.setOrder(userFactors.indexOf(factor));
        new Thread(() ->
        {
            Log.d("add", factor.getPackageName() + " index " + factor.getOrder());
            Log.d("add", "number of Shortcuts:  " + factor.getUserApp().getShortCuts().size());
            Log.d("add", "Shortcuts:  " + factor.getUserApp().getShortCuts());
            daoReference.insert(factor);
            adapter.addFactorBroadcast(userFactors.indexOf(factor));
            adapter.activity.runOnUiThread(()-> adapter.notifyItemInserted(factor.getOrder()));
        }).start();
        updateOrders();
    }

    //remove factor from home
    public void removeFromHome(Factor factor)
    {
        new Thread(() ->
        {
            userFactors.remove(factor);
            daoReference.delete(factor);
            adapter.activity.runOnUiThread(adapter::notifyDataSetChanged);
            updateOrders();
        }).start();
    }

    //check if the app is added to home
    public boolean isAppPinned(UserApp app)
    {
        ArrayList<Factor> copyFactors = new ArrayList<>(userFactors);
        for (Factor f : copyFactors)
            if (f.getPackageName().equals(app.getPackageName())) return true;

        return false;
    }

    //resize a factor
    public boolean resizeFactor(Factor factor, int size)
    {
        factor.setSize(size);
        return updateFactor(factor);
    }

    //update factor info after editing
    private boolean updateFactor(Factor factor)
    {
        if (!userFactors.contains(factor))
            return false;
        else
        {
            int position = userFactors.indexOf(factor);
            factor.setOrder(position);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
                factor.setShortcuts(getShortcutsFromFactor(factor));

            new Thread(()->
            {
                daoReference.updateFactorInfo(factor);
                adapter.activity.runOnUiThread(() -> adapter.notifyItemChanged(position));
            }).start();
        }
        return true;
    }

    //update factor info after its app has changed
    public void updateFactor(UserApp app)
    {
        ArrayList<Factor> factorsToUpdate = getFactorsByPackage(app);
        for (Factor f : factorsToUpdate)
        {
            if (userFactors.contains(f))
            {
                loadIcon(f);
                f.setLabelOld(app.getLabelOld());
                f.setLabelNew(app.getLabelNew());
                f.setUserApp(app);
                new Thread(()->
                {
                    daoReference.updateFactorInfo(f);
                    adapter.activity.runOnUiThread(() -> adapter.notifyItemChanged(userFactors.indexOf(f)));
                }).start();
            }
        }

    }

    //update the index of each factor after reordering
    public void updateOrders()
    {
        new Thread(() ->
        {
            for (Factor f: userFactors)
            {
                f.setOrder(userFactors.indexOf(f));
                daoReference.updateFactorOrder(f.getPackageName(), f.getOrder());
            }
        }).start();
    }

    //remove all related factors from home given an app
    public void remove(UserApp app)
    {
        new Thread(()->
        {
            ArrayList<Factor> factorsToRemove = getFactorsByPackage(app);
            for (Factor f : factorsToRemove)
            {
                if (userFactors.contains(f)) removeFromHome(f);
            }
        }).start();

    }

    //search for factors related to the same app (deprecate, as currently each app can only have one factor pinned)
    private ArrayList<Factor> getFactorsByPackage(UserApp app)
    {
        ArrayList<Factor> factorsToRemove = new ArrayList<>();

        for (Factor f : userFactors)
        {
            if (f.getPackageName().equals(app.getPackageName()))
                factorsToRemove.add(f);
        }

        return factorsToRemove;
    }



    //retrieve the icon for a given factor
    public void loadIcon(Factor factor)
    {
        try
        {
            if (packageManager.getApplicationInfo(factor.getPackageName(), 0).enabled)
                factor.setIcon(packageManager.getApplicationIcon(factor.getPackageName()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            new Thread(() -> daoReference.delete(factor)).start();
        }
    }


    //received notification
    public void onReceivedNotification(Intent intent, UserApp app, Payload payload)
    {
        adapter.onReceivedNotification(intent, app, payload);
    }

    //cleared notification
    public void onClearedNotification(Intent intent, UserApp app, Payload payload)
    {
        adapter.onClearedNotification(intent, app, payload);
    }


    //update UI given the app
    public void clearNotification(UserApp app)
    {
        adapter.clearNotification(app);
    }


    public void invalidate()
    {
        this.adapter.invalidate();
        this.packageManager = null;
        this.appSettings = null;
        this.adapter = null;
    }


    //find shortcuts related to a factor
    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    public ArrayList<AppShortcut> getShortcutsFromFactor(Factor factor)
    {
        ArrayList<AppShortcut> shortcuts = new ArrayList<>();

        if (launcherApps == null || !launcherApps.hasShortcutHostPermission())
            return shortcuts;

        shortcutQuery.setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC|
                LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST|
                LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED);

        shortcutQuery.setPackage(factor.getPackageName());
        List<ShortcutInfo> s = launcherApps.getShortcuts(shortcutQuery, Process.myUserHandle());
        if (s != null && !s.isEmpty())
        {
            for (ShortcutInfo info : s)
            {
                Drawable icon = launcherApps.getShortcutIconDrawable(info, adapter.activity.getResources().getDisplayMetrics().densityDpi);
                View.OnClickListener listener = v -> launcherApps.startShortcut(info.getPackage(), info.getId(), null, null, Process.myUserHandle());
                shortcuts.add(new AppShortcut(!info.isDynamic(), info.getRank(), info.getShortLabel(), icon, listener));
            }
        }
        Collections.sort(shortcuts);
        return shortcuts;

    }
}
