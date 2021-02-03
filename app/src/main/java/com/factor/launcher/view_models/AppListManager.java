package com.factor.launcher.view_models;

import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.*;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import com.factor.launcher.adapters.AppListAdapter;
import com.factor.launcher.database.AppListDatabase;
import com.factor.launcher.fragments.HomeScreenFragment;
import com.factor.launcher.models.AppShortcut;
import com.factor.launcher.models.UserApp;
import com.factor.launcher.util.Constants;
import com.factor.launcher.util.WidgetActivityResultContract;

import java.text.Collator;
import java.util.*;

import static com.factor.launcher.util.Constants.*;

public class AppListManager extends ViewModel
{
    private static final String TAG = "AppListManager";

    private boolean displayHidden = false;

    public boolean isAfterRename = false;

    private final MutableLiveData<ArrayList<UserApp>> appsMutableLiveData = new MutableLiveData<>();

    private final ArrayList<UserApp> userApps = new ArrayList<>();

    private final ArrayList<UserApp> queryApps = new ArrayList<>();

    private final AppListDatabase.AppListDao daoReference;

    private final SharedPreferences factorSharedPreferences;

    public PackageManager packageManager;

    public LauncherApps launcherApps;

    private LauncherApps.ShortcutQuery shortcutQuery;

    private SharedPreferences.Editor editor;

    private FactorManager factorManager;

    public AppListAdapter adapter;

    private final AppWidgetManager appWidgetManager;

    private final AppWidgetHost appWidgetHost;

    private final WidgetActivityResultContract widgetActivityResultContract;

    private final ActivityResultLauncher<Intent> widgetResultLauncher;

    //constructor
    public AppListManager(HomeScreenFragment fragment,
                          ViewGroup background,
                          Boolean isLiveWallpaper)
    {

        this.appWidgetManager = AppWidgetManager.getInstance(fragment.requireActivity());
        this.appWidgetHost = new AppWidgetHost(fragment.requireActivity(), WIDGET_HOST_ID);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
            this.shortcutQuery = new LauncherApps.ShortcutQuery();


        this.packageManager = fragment.requireActivity().getPackageManager();
        this.launcherApps = (LauncherApps) fragment.requireActivity().getSystemService(Context.LAUNCHER_APPS_SERVICE);
        this.adapter = new AppListAdapter(this, userApps, displayHidden, fragment.getActivity());
        this.factorManager = new FactorManager(fragment.requireActivity(), background, packageManager, launcherApps, shortcutQuery, isLiveWallpaper);

        daoReference = AppListDatabase.Companion.getInstance(fragment.requireActivity().getApplicationContext()).appListDao();

        this.factorSharedPreferences = fragment.requireActivity().getSharedPreferences(PACKAGE_NAME + "_FIRST_LAUNCH", Context.MODE_PRIVATE);
        widgetActivityResultContract = new WidgetActivityResultContract();
        widgetResultLauncher = fragment.registerForActivityResult(widgetActivityResultContract, this::handleWidgetResult);

        appsMutableLiveData.setValue(userApps);


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


    public MutableLiveData<ArrayList<UserApp>> getAppsMutableLiveData()
    {
        return appsMutableLiveData;
    }

    //return activity
    public Activity getActivity()
    {
        return this.adapter.activity;
    }

    //return factor manager
    public FactorManager getFactorManager()
    {
        return this.factorManager;
    }

    //load app drawer list
    private void loadApps(Boolean isSaved)
    {
        if (isSaved) //if the app drawer has been loaded before, load from db instead
        {
            new Thread(() ->
            {

                    Intent i = new Intent(Intent.ACTION_MAIN, null);
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    List<ResolveInfo> availableApps = packageManager.queryIntentActivities(i, 0);
                    for (ResolveInfo r : availableApps)
                    {
                        try
                        {
                            if (!r.activityInfo.packageName.equals(PACKAGE_NAME))
                            {
                                UserApp app = daoReference.findByPackage(r.activityInfo.packageName);
                                if (app == null) //package name does not exist in database
                                {
                                    app = new UserApp();
                                    app.setLabelOld((String) r.loadLabel(packageManager));
                                    app.setLabelNew(app.getLabelOld());
                                    app.setPackageName(r.activityInfo.packageName);
                                    app.resetNotifications();
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
                                        app.setShortCuts(getShortcutsFromApp(app));


                                    app.setIcon(r.activityInfo.loadIcon(packageManager));

                                    userApps.add(app);
                                    daoReference.insert(app);
                                }
                                else {
                                    if (doesPackageExist(app) && packageManager.getApplicationInfo(app.getPackageName(), 0).enabled)
                                    {
                                        app.setIcon(r.activityInfo.loadIcon(packageManager));

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
                                            app.setShortCuts(getShortcutsFromApp(app));

                                        userApps.add(app);

                                        app.setPinned(factorManager.isAppPinned(app));

                                        Collections.sort(userApps, first_letter);
                                    }
                                    else
                                        daoReference.delete(app);
                                }
                            }
                        }
                        catch (NullPointerException | PackageManager.NameNotFoundException ex)
                        {
                            ex.printStackTrace();
                        }

                    }
                    if (adapter.activity != null)
                        adapter.activity.runOnUiThread(adapter::notifyDataSetChanged);

            }).start();
        }
        else //if the app drawer is loading for the first time, load all apps with default configuration
        {
            editor = factorSharedPreferences.edit();
            new Thread(() ->
            {

                    Intent i = new Intent(Intent.ACTION_MAIN, null);
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    List<ResolveInfo> availableApps = packageManager.queryIntentActivities(i, 0);
                    for (ResolveInfo r : availableApps)
                    {
                        try
                        {
                            if (!r.activityInfo.packageName.equals(PACKAGE_NAME) && packageManager.getApplicationInfo(r.activityInfo.packageName, 0).enabled) {
                                UserApp app = new UserApp();
                                app.setLabelOld((String) r.loadLabel(packageManager));
                                app.setLabelNew(app.getLabelOld());
                                app.setPackageName(r.activityInfo.packageName);

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
                                    app.setShortCuts(getShortcutsFromApp(app));

                                app.setIcon(r.activityInfo.loadIcon(packageManager));
                                userApps.add(app);
                            }
                        }
                        catch (PackageManager.NameNotFoundException | NullPointerException ex)
                        {
                            ex.printStackTrace();
                        }

                    }
                    Collections.sort(userApps, first_letter);

                    daoReference.insertAll(userApps);

                    if (adapter.activity != null)
                        adapter.activity.runOnUiThread(adapter::notifyDataSetChanged);

                    editor.putBoolean("saved", true);
                    editor.apply();


            }).start();
        }
    }

    //pin & unpin
    public boolean changePin(UserApp userApp)
    {
        userApp.changePinnedState();
        if (userApp.isPinned())
            factorManager.addToHome(userApp);

        new Thread(() ->
        {
            daoReference.updateAppInfo(userApp);
            adapter.activity.runOnUiThread(() -> adapter.notifyItemChanged(userApps.indexOf(userApp)));
        }).start();

        return userApps.contains(userApp);
    }

    //set app to hidden
    public boolean hideApp(UserApp userApp)
    {
        if (!userApps.contains(userApp)) return false;

        userApp.setHidden(true);
        new Thread(() ->
        {
            daoReference.updateAppInfo(userApp);
            adapter.activity.runOnUiThread(() -> adapter.notifyItemChanged(userApps.indexOf(userApp)));
        }).start();
        return true;
    }

    //set app to not hidden
    public boolean showApp(UserApp userApp)
    {
        if (!userApps.contains(userApp)) return false;

        userApp.setHidden(false);
        new Thread(() ->
        {
            daoReference.updateAppInfo(userApp);
            adapter.activity.runOnUiThread(() -> adapter.notifyItemChanged(userApps.indexOf(userApp)));
        }).start();
        return true;
    }

    //check if package exists
    private boolean doesPackageExist(UserApp a)
    {
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> availableApps = packageManager.queryIntentActivities(i, 0);
        for (ResolveInfo r : availableApps) {
            if (!r.activityInfo.packageName.equals(PACKAGE_NAME)
                    && r.activityInfo.packageName.equals(a.getPackageName())) {
                return true;
            }
        }
        return false;
    }


    //remove app from the list only if package no longer exists
    public void removeApp(UserApp app)
    {
        if (!doesPackageExist(app))
        {
                new Thread(() ->
                {
                    int position = userApps.indexOf(app);
                    userApps.remove(app);
                    daoReference.delete(app);
                    adapter.activity.runOnUiThread(() -> adapter.notifyItemRemoved(position));
                }).start();
                factorManager.remove(app);

        }
    }

    //remove app from database
    public void removeAppFromDB(UserApp app)
    {
        new Thread(() -> daoReference.delete(app)).start();
    }


    //return cache list size
    public int getListSize()
    {
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        return (packageManager.queryIntentActivities(i, 0)).size();
    }


    //add app after receiving PACKAGE_ADDED broadcast
    public void addApp(UserApp app)
    {
        if (doesPackageExist(app) && !isAppDuplicate(app))
        {
            new Thread(() ->
            {
                try
                {
                    ApplicationInfo info = packageManager.getApplicationInfo(app.getPackageName(), 0);
                    app.setIcon(packageManager.getApplicationIcon(app.getPackageName()));
                    app.setLabelOld((String) packageManager.getApplicationLabel(info));

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
                        app.setShortCuts(getShortcutsFromApp(app));

                    app.setLabelNew(app.getLabelOld());
                    daoReference.insert(app);
                    userApps.add(app);
                    Collections.sort(userApps, first_letter);

                    adapter.activity.runOnUiThread(() -> adapter.notifyItemInserted(userApps.indexOf(app)));
                }
                catch (PackageManager.NameNotFoundException e)
                {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    //return true if app exists
    private boolean isAppDuplicate(UserApp app)
    {
        ArrayList<UserApp> copyList = new ArrayList<>(userApps);
        for (UserApp userApp : copyList)
        {
            if (userApp.getPackageName().equals(app.getPackageName()))
                return true;
        }
        return false;
    }

    //update app info in database
    public void updateApp(UserApp app)
    {
        UserApp appToUpdate;
        try {

            //only update the app if the package exists in system and is enabled
            if (doesPackageExist(app) && packageManager.getApplicationInfo(app.getPackageName(), 0).enabled)
            {
                if (userApps.contains(app))
                {
                    int position = userApps.indexOf(app);
                    appToUpdate = userApps.get(position);
                    new Thread(() ->
                    {
                        try
                        {
                            ApplicationInfo info = packageManager.getApplicationInfo(appToUpdate.getPackageName(), 0);
                            userApps.get(position).setIcon(packageManager.getApplicationIcon(appToUpdate.getPackageName()));
                            userApps.get(position).setLabelOld((String) packageManager.getApplicationLabel(info));

                            //retrieve shortcuts on api 25 and higher
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
                                userApps.get(position).setShortCuts(getShortcutsFromApp(app));

                            daoReference.updateAppInfo(appToUpdate);
                            Collections.sort(userApps, first_letter);
                            int newPosition = userApps.indexOf(app);
                            adapter.activity.runOnUiThread(() ->
                            {

                                if (position != newPosition) //the app's position is changed
                                {
                                    adapter.notifyItemRemoved(position);
                                    adapter.notifyItemInserted(newPosition);
                                }
                                else
                                    adapter.notifyItemChanged(newPosition); //the app's position is unchanged in the list

                                //if updating app list after renaming an app, broadcast request to scroll to the app's new position
                                if (isAfterRename)
                                {
                                    adapter.renameBroadCast(newPosition);
                                    isAfterRename = false;
                                }
                            });
                        }
                        catch (PackageManager.NameNotFoundException ignored) {}
                    }).start();

                    //if app is pinned, update its tile
                    if (appToUpdate.isPinned())
                        factorManager.updateFactor(appToUpdate);

                }
                else addApp(app);
            }
            else
            {
                //the package might exist, but it's disabled by the user
                if (userApps.contains(app))
                    removeApp(app); //remove it from the app drawer if it's disabled
            }
        }
        catch (PackageManager.NameNotFoundException e)
        {
            Log.d(TAG, e.getMessage());
        }

    }


    //remove from home
    public void unPin(String packageName)
    {
        for (UserApp app : userApps) {
            if (app.getPackageName().equals(packageName)) {
                changePin(app);
                break;
            }
        }
    }

    //search bar filter app list
    public void findPosition(RecyclerView rc, String newText)
    {
        if (rc != null && rc.getLayoutManager() != null)
        {
            RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(adapter.activity)
            {
                @Override protected int getVerticalSnapPreference()
                {
                    return LinearSmoothScroller.SNAP_TO_START;
                }
            };
            ArrayList<UserApp> copyList = new ArrayList<>(userApps);
            for (UserApp a : copyList)
            {
                if (a.getSearchReference().contains(newText.toLowerCase()))
                {
                    smoothScroller.setTargetPosition(copyList.indexOf(a));
                    rc.getLayoutManager().startSmoothScroll(smoothScroller);
                    return;
                }
            }
            smoothScroller.setTargetPosition(0);
            rc.getLayoutManager().startSmoothScroll(smoothScroller);
        }

    }


    //change display mode, return a new adapter
    public AppListAdapter setDisplayHidden(boolean displayHidden)
    {
        this.displayHidden = displayHidden;
        Activity activity = adapter.activity;
        this.adapter = new AppListAdapter(this, userApps, displayHidden, activity);
        return adapter;
    }


    //not needed anymore
    public void invalidate()
    {
        if (this.factorManager != null)
            this.factorManager.invalidate();

        if (this.adapter != null)
            this.adapter.invalidate();
        this.adapter = null;
        this.factorManager = null;
    }

    //retrieve list of app shortcuts
    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    public ArrayList<AppShortcut> getShortcutsFromApp(UserApp app)
    {
        ArrayList<AppShortcut> shortcuts = new ArrayList<>();

        if (launcherApps == null || !launcherApps.hasShortcutHostPermission())
            return shortcuts;

        shortcutQuery.setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC|
                LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST|
                LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED);

        shortcutQuery.setPackage(app.getPackageName());
        List<ShortcutInfo> s = launcherApps.getShortcuts(shortcutQuery, Process.myUserHandle());
        if (s != null && !s.isEmpty())
        {
            for (ShortcutInfo info : s)
            {
                Drawable icon = factorManager.launcherApps.getShortcutIconDrawable(info, getActivity().getResources().getDisplayMetrics().densityDpi);
                View.OnClickListener listener = v -> launcherApps.startShortcut(info.getPackage(), info.getId(), null, null, Process.myUserHandle());
                shortcuts.add(new AppShortcut(info.getShortLabel(), icon, listener));
            }
        }
        return shortcuts;

    }


    //get display mode
    public boolean isDisplayingHidden()
    {
        return this.displayHidden;
    }

    //received notifications
    public void onReceivedNotification(Intent intent)
    {
        adapter.onReceivedNotification(intent);
    }

    //cleared notification
    public void onClearedNotification(Intent intent)
    {
        adapter.onClearedNotification(intent);
    }

    //clear all notifications
    public void clearAllNotifications()
    {
        adapter.clearAllNotifications();
    }


    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    public void updateShortcuts()
    {
        ArrayList<UserApp> copyApps = new ArrayList<>(userApps);
        for (UserApp app : copyApps)
        {
            userApps.get(userApps.indexOf(app)).setShortCuts(getShortcutsFromApp(app));
        }
    }


    //return the app at a given position (not the array position)
    public UserApp getUserApp(int position)
    {
        try
        {
            UserApp appToFind = userApps.get(position);
            ArrayList<UserApp> copyApps = new ArrayList<>(userApps);
            int newPosition = 0;
            queryApps.clear();
            if (!displayHidden)
            {
                for (UserApp app : copyApps)
                {
                    if (!app.isHidden())
                        queryApps.add(app);
                }
                if (!appToFind.isHidden())
                    newPosition = queryApps.indexOf(appToFind);
            }
            else
            {
                for (UserApp app : copyApps)
                {
                    if (app.isHidden())
                        queryApps.add(app);
                }
                if (appToFind.isHidden())
                    newPosition = queryApps.indexOf(appToFind);
            }
            if (queryApps.size() > 0)
                return queryApps.get(newPosition);
            else
                return new UserApp();
        }
        catch (ConcurrentModificationException | NullPointerException e)
        {
            return new UserApp();
        }

    }


    //launch pick widget intent
    public void launchPickWidgetIntent()
    {
        int appWidgetId = appWidgetHost.allocateAppWidgetId();
        Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        pickIntent.putExtra(Constants.WIDGET_KEY, Constants.REQUEST_PICK_WIDGET);
        widgetResultLauncher.launch(widgetActivityResultContract.createIntent(adapter.activity, pickIntent));
    }


    //receive activity result from widget intent
    private void handleWidgetResult(Intent intent)
    {
        if (intent.getIntExtra(Constants.WIDGET_RESULT_KEY, -1) == Activity.RESULT_OK)
        {
            Log.d("widget", "result: ok");
            if (intent.getIntExtra(Constants.WIDGET_KEY, -1) == REQUEST_PICK_WIDGET)
                conFigureWidget(intent);
            else if (intent.getIntExtra(Constants.WIDGET_KEY, -1) == REQUEST_CREATE_WIDGET)
                createWidget(intent);
        }
        else if (intent.getIntExtra(Constants.WIDGET_RESULT_KEY, -1) == Activity.RESULT_CANCELED)
        {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (appWidgetId != -1) appWidgetHost.deleteAppWidgetId(appWidgetId);
        }
    }

    //request to configure app widget
    private void conFigureWidget(Intent data)
    {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        AppWidgetProviderInfo appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
        //requestBindWidget(appWidgetId, appWidgetInfo);
        if (appWidgetInfo.configure != null)
        {
            Log.d("widget", "configure");
            Intent createIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            createIntent.setComponent(appWidgetInfo.configure);
            createIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            createIntent.putExtra(Constants.WIDGET_KEY, Constants.REQUEST_CREATE_WIDGET);
            widgetResultLauncher.launch(widgetActivityResultContract.createIntent(adapter.activity, createIntent));
        }
        else {
            createWidget(data);
        }
    }

    //create appWidgetView
    private void createWidget(Intent data)
    {
        Log.d("widget", "create");
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        AppWidgetProviderInfo appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
        requestBindWidget(appWidgetId, appWidgetInfo);
        AppWidgetHostView hostView = appWidgetHost.createView(adapter.activity, appWidgetId, appWidgetInfo);
        hostView.setAppWidget(appWidgetId, appWidgetInfo);
        factorManager.addWidget(hostView);
    }


    //todo: doesn't work
    public void requestBindWidget(int appWidgetId, AppWidgetProviderInfo info)
    {
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.provider);
        // This is the options bundle discussed above
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS, info.configure);
        adapter.activity.startActivityForResult(intent, REQUEST_BIND_WIDGET);
    }


}
