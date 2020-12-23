package com.factor.launcher.managers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.*;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.*;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import com.factor.launcher.R;
import com.factor.launcher.database.AppListDatabase;
import com.factor.launcher.databinding.AppListItemBinding;
import com.factor.launcher.fragments.HomeScreenFragment;
import com.factor.launcher.models.NotificationHolder;
import com.factor.launcher.models.UserApp;
import com.factor.launcher.util.Constants;
import com.factor.launcher.util.Payload;
import com.factor.launcher.util.WidgetActivityResultContract;


import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static com.factor.launcher.util.Constants.*;

public class AppListManager
{
    private final String TAG = "AppListManager";

    private boolean displayHidden = false;

    private boolean isAfterRename = false;

    private final ArrayList<UserApp> userApps = new ArrayList<>();

    private final ArrayList<UserApp> queryApps = new ArrayList<>();

    private final Activity activity;

    private final AppListDatabase appListDatabase;

    private final SharedPreferences factorSharedPreferences;

    private final PackageManager packageManager;

    private final LauncherApps launcherApps;

    private LauncherApps.ShortcutQuery shortcutQuery;

    private SharedPreferences.Editor editor;

    private FactorManager factorManager;

    public AppListAdapter adapter;

    private final AppWidgetManager appWidgetManager;

    private final AppWidgetHost appWidgetHost;

    private final WidgetActivityResultContract widgetActivityResultContract;

    private final ActivityResultLauncher<Intent> widgetResultLauncher;

    //constructor
    public AppListManager(HomeScreenFragment fragment, ViewGroup background, Boolean isLiveWallpaper)
    {
        this.activity = fragment.requireActivity();

        this.appWidgetManager = AppWidgetManager.getInstance(activity.getApplicationContext());
        this.appWidgetHost = new AppWidgetHost(activity.getApplicationContext(), WIDGET_HOST_ID);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
            this.shortcutQuery = new LauncherApps.ShortcutQuery();


        this.packageManager = activity.getPackageManager();
        this.launcherApps = (LauncherApps) activity.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        this.adapter = new AppListAdapter();
        this.factorManager = new FactorManager(activity, background, packageManager, launcherApps, shortcutQuery, isLiveWallpaper, appWidgetHost, appWidgetManager);

        this.appListDatabase = AppListDatabase.Companion.getInstance(activity.getApplicationContext());

        this.factorSharedPreferences = activity.getSharedPreferences(PACKAGE_NAME + "_FIRST_LAUNCH", Context.MODE_PRIVATE);

        loadApps(factorSharedPreferences.getBoolean("saved", false));

        widgetActivityResultContract = new WidgetActivityResultContract();
        widgetResultLauncher = fragment.registerForActivityResult(widgetActivityResultContract, this::handleWidgetResult);

        this.appWidgetHost.startListening();
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
                try
                {
                    Intent i = new Intent(Intent.ACTION_MAIN, null);
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    List<ResolveInfo> availableApps = packageManager.queryIntentActivities(i, 0);
                    for (ResolveInfo r : availableApps)
                    {
                        if (!r.activityInfo.packageName.equals(PACKAGE_NAME))
                        {
                            UserApp app = appListDatabase.appListDao().findByPackage(r.activityInfo.packageName);
                            //noinspection ConstantConditions
                            if (app == null) //package name does not exist in database
                            {
                                app = new UserApp();
                                app.setLabelOld((String) r.loadLabel(packageManager));
                                app.setLabelNew(app.getLabelOld());
                                app.setPackageName(r.activityInfo.packageName);

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
                                    app.setShortCuts(getShortcutsFromApp(app));


                                app.icon = r.activityInfo.loadIcon(packageManager);

                                userApps.add(app);
                                appListDatabase.appListDao().insert(app);
                            }
                            else
                            {
                                if (doesPackageExist(app) && packageManager.getApplicationInfo(app.getPackageName(), 0).enabled)
                                {
                                    app.icon = r.activityInfo.loadIcon(packageManager);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                                        app.setShortCuts(getShortcutsFromApp(app));
                                    }
                                    userApps.add(app);
                                    app.setPinned(factorManager.isAppPinned(app));
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
        else //if the app drawer is loading for the first time, load all apps with default configuration
        {
            editor = factorSharedPreferences.edit();
            new Thread(() ->
            {
                try
                {
                    Intent i = new Intent(Intent.ACTION_MAIN, null);
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    List<ResolveInfo> availableApps = packageManager.queryIntentActivities(i, 0);
                    for (ResolveInfo r : availableApps)
                    {
                        if (!r.activityInfo.packageName.equals(PACKAGE_NAME) && packageManager.getApplicationInfo(r.activityInfo.packageName, 0).enabled)
                        {
                            UserApp app = new UserApp();
                            app.setLabelOld((String) r.loadLabel(packageManager));
                            app.setLabelNew(app.getLabelOld());
                            app.setPackageName(r.activityInfo.packageName);

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
                                app.setShortCuts(getShortcutsFromApp(app));

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

    //set app to hidden
    private boolean hideApp(UserApp userApp)
    {
        if (!userApps.contains(userApp)) return false;

        userApp.setHidden(true);
        new Thread(() ->
        {
            appListDatabase.appListDao().updateAppInfo(userApp);
            activity.runOnUiThread(() -> adapter.notifyItemChanged(userApps.indexOf(userApp)));
        }).start();
        return true;
    }

    //set app to not hidden
    private boolean showApp(UserApp userApp)
    {
        if (!userApps.contains(userApp)) return false;

        userApp.setHidden(false);
        new Thread(() ->
        {
            appListDatabase.appListDao().updateAppInfo(userApp);
            activity.runOnUiThread(() -> adapter.notifyItemChanged(userApps.indexOf(userApp)));
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

    //find app object given package name
    private UserApp findAppByPackage(String packageName)
    {
        for (UserApp a : userApps)
            if (a.getPackageName().equals(packageName)) return a;

        return new UserApp();
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

    //return true if app exists
    private boolean isAppDuplicate(UserApp app)
    {
        for (UserApp userApp : userApps) {
            if (userApp.getPackageName().equals(app.getPackageName())) {
                return true;
            }
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

                            appListDatabase.appListDao().updateAppInfo(appToUpdate);
                            userApps.sort(first_letter);
                            int newPosition = userApps.indexOf(app);
                            activity.runOnUiThread(() ->
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
                                    renameBroadCast(newPosition);
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
        RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(activity)
        {
            @Override protected int getVerticalSnapPreference()
            {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };
        for (UserApp a : userApps)
        {
            if (a.getSearchReference().contains(newText.toLowerCase()))
            {
                smoothScroller.setTargetPosition(userApps.indexOf(a));
                Objects.requireNonNull((LinearLayoutManager)rc.getLayoutManager()).startSmoothScroll(smoothScroller);
                return;
            }
        }
        smoothScroller.setTargetPosition(0);
        Objects.requireNonNull((LinearLayoutManager)rc.getLayoutManager()).startSmoothScroll(smoothScroller);
    }

    //edit app dialog
    public void renameApp(UserApp app, String newLabel)
    {
        if (!userApps.contains(app)) return;

        app.setCustomized(true);
        app.setLabelNew(newLabel);
        isAfterRename = true;
        updateApp(app);
    }

    //reset app's name back to original label
    public void resetAppEdit(UserApp app)
    {
        if (!userApps.contains(app)) return;

        app.setCustomized(false);
        app.setLabelNew(app.getLabelOld());
        isAfterRename = true;
        updateApp(app);
    }

    //change display mode, return a new adapter
    public AppListAdapter setDisplayHidden(boolean displayHidden)
    {
        this.displayHidden = displayHidden;
        this.adapter = new AppListAdapter();
        return adapter;
    }

    //invalidate resources on destroy
    public void invalidate()
    {
        if (this.appWidgetHost != null)
            this.appWidgetHost.stopListening();
        if (this.factorManager != null)
            this.factorManager.invalidate();

        this.adapter = null;
        this.factorManager = null;
    }

    //retrieve list of app shortcuts
    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    public List<ShortcutInfo> getShortcutsFromApp(UserApp app)
    {

        if (launcherApps == null || !launcherApps.hasShortcutHostPermission())
            return new ArrayList<>(0);


        shortcutQuery.setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC|
                LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST|
                LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED);

        shortcutQuery.setPackage(app.getPackageName());
        List<ShortcutInfo> shortcuts = launcherApps.getShortcuts(shortcutQuery, Process.myUserHandle());
        if (shortcuts == null || shortcuts.isEmpty())
            return new ArrayList<>(0);
        else
            return shortcuts;
    }

    //start app shortcut
    private void startShortCut(ShortcutInfo shortcutInfo)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
            launcherApps.startShortcut(shortcutInfo.getPackage(), shortcutInfo.getId(), null, null, Process.myUserHandle());
    }

    //send a broadcast after renaming an app
    private void renameBroadCast(int position)
    {
        Intent intent = new Intent();
        intent.setAction(Constants.BROADCAST_ACTION_RENAME);
        intent.putExtra(Constants.RENAME_KEY, position);
        activity.sendBroadcast(intent);
    }

    //get display mode
    public boolean isDisplayingHidden()
    {
        return this.displayHidden;
    }

    //received notifications
    public void onReceivedNotification(Intent intent)
    {
        UserApp app = findAppByPackage(intent.getStringExtra(Constants.NOTIFICATION_INTENT_PACKAGE_KEY));
        NotificationHolder notificationHolder =
                new NotificationHolder(intent.getIntExtra(Constants.NOTIFICATION_INTENT_ID_KEY, 0),
                        intent.getStringExtra(Constants.NOTIFICATION_INTENT_TITLE_TEXT_KEY),
                        intent.getStringExtra(Constants.NOTIFICATION_INTENT_CONTENT_TEXT_KEY));
        Payload payload = new Payload(notificationHolder, Payload.NOTIFICATION_RECEIVED);
        if (userApps.contains(app))
        {
            if (app.incrementNotificationCount(notificationHolder))
                adapter.notifyItemChanged(userApps.indexOf(app), payload);

            if (app.isPinned())
                factorManager.onReceivedNotification(intent, app, payload);


            Log.d("payload", app.getNotificationTitle() + " created payload");
        }
    }

    //cleared notification
    public void onClearedNotification(Intent intent)
    {
        UserApp app = findAppByPackage(intent.getStringExtra(Constants.NOTIFICATION_INTENT_PACKAGE_KEY));
        NotificationHolder notificationHolder =
                new NotificationHolder(intent.getIntExtra(Constants.NOTIFICATION_INTENT_ID_KEY, 0), "", "");
        Payload payload = new Payload(notificationHolder, Payload.NOTIFICATION_CLEARED);
        if (userApps.contains(app))
        {
            Log.d("payload", app.getPackageName() + " created payload");
            if (app.decreaseNotificationCount(payload.getNotificationHolder().getId()))
                adapter.notifyItemChanged(userApps.indexOf(app), payload);

            if (app.isPinned())
                factorManager.onClearedNotification(intent, app, payload);
        }
    }

    //return the app at a given position (not the array position)
    public UserApp getUserApp(int position)
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

    //load icon for the app
    private void loadIcon(UserApp app)
    {
        try
        {
            if (packageManager.getApplicationInfo(app.getPackageName(), 0).enabled)
                app.setIcon(packageManager.getApplicationIcon(app.getPackageName()));
        }
        catch (Exception e)
        {
           //unable to load icon for the app
            new Thread(() -> appListDatabase.appListDao().delete(app)).start();
        }
    }


    //launch pick widget intent
    public void launchPickWidgetIntent()
    {
        int appWidgetId = appWidgetHost.allocateAppWidgetId();
        Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        pickIntent.putExtra(Constants.WIDGET_KEY, Constants.REQUEST_PICK_WIDGET);
        widgetResultLauncher.launch(widgetActivityResultContract.createIntent(activity.getApplicationContext(), pickIntent));
    }


    //receive activity result from widget intent
    private void handleWidgetResult(Intent intent)
    {
        if (intent.getIntExtra(WIDGET_RESULT_KEY, -1) == Activity.RESULT_OK)
        {
            if (intent.getIntExtra(WIDGET_KEY, -1) == REQUEST_PICK_WIDGET)
                conFigureWidget(intent);
            else if (intent.getIntExtra(WIDGET_KEY, -1) == REQUEST_CREATE_WIDGET)
                createWidget(intent);
            else if (intent.getIntExtra(WIDGET_KEY, -1) == REQUEST_BIND_WIDGET)
            {

            }
        }
        else if (intent.getIntExtra(Constants.WIDGET_RESULT_KEY, -1) == Activity.RESULT_CANCELED)
        {
            Log.d("result_widget", "not okay");
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (appWidgetId != -1) appWidgetHost.deleteAppWidgetId(appWidgetId);
        }

        Log.d("result_widget","request code: " + intent.getIntExtra(WIDGET_KEY, -1));
    }


    //request to configure app widget
    private void conFigureWidget(Intent data)
    {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        AppWidgetProviderInfo appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);

        if (appWidgetInfo.configure != null)
        {
            Log.d("result_widget", "configure");
            Intent createIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            createIntent.setComponent(appWidgetInfo.configure);
            createIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            createIntent.putExtra(Constants.WIDGET_KEY, Constants.REQUEST_CREATE_WIDGET);
            requestBindWidget(appWidgetId, appWidgetInfo);
            try
            {
                widgetResultLauncher.launch(widgetActivityResultContract.createIntent(activity.getApplicationContext(), createIntent));
            }
            catch (SecurityException exception)
            {
                Toast.makeText(activity.getApplicationContext(), "Failed to configure widget. Error message: " + exception.getMessage(), Toast.LENGTH_LONG).show();
            }

        }
        else createWidget(data);
    }

    //create appWidgetView
    private void createWidget(Intent data)
    {
        Log.d("result_widget", "create");
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        factorManager.addWidget(appWidgetId);
    }

    //request permission to bind widget
    //todo: handle result
    public void requestBindWidget(int appWidgetId, AppWidgetProviderInfo info)
    {
        if (!appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, info.provider) || !appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, info.configure))
        {
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.provider);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS, info.configure);
            intent.putExtra(WIDGET_KEY, REQUEST_BIND_WIDGET);
            Log.d("result_widget", "request");
            widgetResultLauncher.launch(widgetActivityResultContract.createIntent(activity.getApplicationContext(), intent));
        }
    }




    //adapter for app drawer
    public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppListViewHolder>
    {
        @SuppressLint("ClickableViewAccessibility")
        @NonNull
        @Override
        public AppListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            int id;
            if (displayHidden)
                id = viewType == 1 ? R.layout.hidden_app : R.layout.app_list_item;
            else
                id = viewType == 1 ? R.layout.app_list_item : R.layout.hidden_app;


            View view = LayoutInflater.from(parent.getContext()).inflate(id, parent, false);

            activity.registerForContextMenu(view);

            AppListViewHolder holder =  new AppListViewHolder(view);

            assert holder.binding != null;
            if (holder.binding instanceof AppListItemBinding)
            {
                ((AppListItemBinding)holder.binding).labelEdit.setOnFocusChangeListener((v, hasFocus) -> {
                    if (hasFocus)
                        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                    else
                        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                });

                view.setOnCreateContextMenuListener((menu, v, menuInfo) ->
                {
                    MenuInflater inflater = activity.getMenuInflater();
                    inflater.inflate(R.menu.app_list_item_menu, menu);
                    if (((AppListItemBinding)holder.binding).getUserApp().isPinned())
                        menu.getItem(0).setEnabled(false);

                    //add to home & remove from home
                    menu.getItem(0).setOnMenuItemClickListener(item -> changePin(((AppListItemBinding)holder.binding).getUserApp()));

                    //edit
                    SubMenu sub = menu.getItem(1).getSubMenu();

                    //rename
                    sub.getItem(0).setOnMenuItemClickListener(item ->
                    {
                        holder.enterEditMode(((AppListItemBinding)holder.binding));
                        return true;
                    });
                    //hide
                    MenuItem hide = sub.getItem(1);
                    if (((AppListItemBinding)holder.binding).getUserApp().isHidden())
                        hide.setTitle("Show");
                    else hide.setTitle("Hide");
                    hide.setOnMenuItemClickListener(item -> !((AppListItemBinding)holder.binding).getUserApp().isHidden() ?
                            hideApp(((AppListItemBinding)holder.binding).getUserApp()) : showApp(((AppListItemBinding)holder.binding).getUserApp()));
                    //info
                    menu.getItem(2).setOnMenuItemClickListener(item ->
                    {
                        view.getContext().startActivity(
                                new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.parse("package:"+((AppListItemBinding)holder.binding).getUserApp().getPackageName())));
                        return true;
                    });

                    //uninstall
                    menu.getItem(3).setOnMenuItemClickListener(item ->
                    {
                        view.getContext().startActivity(new Intent(Intent.ACTION_DELETE, Uri.parse("package:"+((AppListItemBinding)holder.binding).getUserApp().getPackageName()))
                                .putExtra(Intent.EXTRA_RETURN_RESULT, true));
                        return true;
                    });
                });


                view.setOnTouchListener((v, event) ->
                {
                    switch(event.getAction())
                    {
                        case MotionEvent.ACTION_DOWN:
                            ((AppListItemBinding)holder.binding).itemHost.animateToClickedState();
                            ((AppListItemBinding)holder.binding).itemBackground.animate().alpha(1).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(150);
                            return false;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            ((AppListItemBinding)holder.binding).itemHost.animateBackFromClickedState();
                            ((AppListItemBinding)holder.binding).itemBackground.animate().alpha(0).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(150);
                            return false;
                    }
                    return false;
                });

            }


            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull AppListViewHolder holder, int position)
        {
            holder.bindApp(userApps.get(position));
        }


        @Override
        public void onViewRecycled(@NonNull AppListViewHolder holder)
        {
            super.onViewRecycled(holder);
            activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        }

        @Override
        public void onBindViewHolder(@NonNull AppListViewHolder holder, int position, @NonNull List<Object> payloads)
        {
            if (!payloads.isEmpty())
            {
                Log.d("payload", "received");
                AppListItemBinding binding = (AppListItemBinding)holder.binding;

                if (!(payloads.get(0) instanceof Payload))
                    onBindViewHolder(holder, position);
                else {
                    //after receiving notification, change the notification counter
                    assert binding != null;
                    UserApp appToChange = binding.getUserApp();
                    if (appToChange.getCurrentNotifications().size() < 1 || appToChange.isBeingEdited())
                        binding.notificationCount.setVisibility(View.GONE);
                    else if (appToChange.getCurrentNotifications().size() > 0 || !appToChange.isBeingEdited())
                    {
                        binding.notificationCount.setVisibility(View.VISIBLE);
                        binding.notificationCount.setText(appToChange.retrieveNotificationCount());
                    }
                }
            }
            else
                onBindViewHolder(holder, position);
        }

        @Override
        public int getItemCount()
        {
            return userApps.size();
        }

        @Override
        public int getItemViewType(int position)
        {
            if (userApps.get(position).isHidden())
                return 0; //do not show
            else
                return 1; //show
        }

        class AppListViewHolder extends RecyclerView.ViewHolder
        {
            private final ViewDataBinding binding;
            public AppListViewHolder(@NonNull View itemView)
            {
                super(itemView);
                binding = DataBindingUtil.bind(itemView);
            }

            public void bindApp(UserApp app)
            {
                if (binding instanceof AppListItemBinding)
                {
                    AppListItemBinding appBinding = (AppListItemBinding)binding;
                    appBinding.setUserApp(app);
                    appBinding.labelEdit.setText(app.getLabelNew());
                    try
                    {
                        appBinding.icon.setImageDrawable(app.getIcon());
                    }
                    catch (kotlin.UninitializedPropertyAccessException ex)
                    {
                        loadIcon(app);
                        appBinding.icon.setImageDrawable(app.getIcon());
                    }


                    appBinding.label.setVisibility(app.visibilityLabel());
                    appBinding.labelEdit.setVisibility(app.visibilityEditing());
                    appBinding.editButtonGroup.setVisibility(app.visibilityEditing());
                    appBinding.notificationCount.setVisibility(app.visibilityNotificationCount());

                    if (!app.isBeingEdited())
                    {
                        setOnClickListener(app);
                        if (app.getCurrentNotifications().size() > 0)
                            appBinding.notificationCount.setText(app.retrieveNotificationCount());
                    }
                    else
                    {
                        removeOnClickListener();
                        appBinding.labelEdit.setText(app.getLabelNew());
                        appBinding.cancelEditButton.setOnClickListener(view -> exitEditMode(app));
                        appBinding.resetEditButton.setOnClickListener(view -> resetEditMode(app));
                        appBinding.confirmEditButton.setOnClickListener(view ->
                        {
                            String newName = Objects.requireNonNull(appBinding.labelEdit.getText()).toString();
                            if (newName.isEmpty())
                                exitEditMode(app);
                            else if (app.isCustomized() && newName.equals(app.getLabelNew()))
                                exitEditMode(app);
                            else if (!app.isCustomized() && newName.equals(app.getLabelOld()))
                                exitEditMode(app);
                            else
                            {
                                exitEditMode(app);
                                renameApp(app, newName);
                            }
                        });
                    }

                }
            }


            public void enterEditMode(AppListItemBinding binding)
            {
                activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                UserApp app =  binding.getUserApp();
                app.setBeingEdited(true);
                removeOnClickListener();
                binding.labelEdit.setText(app.getLabelNew());
                binding.cancelEditButton.setOnClickListener(view -> exitEditMode(app));
                binding.resetEditButton.setOnClickListener(view -> resetEditMode(app));
                binding.confirmEditButton.setOnClickListener(view ->
                {
                    String newName = Objects.requireNonNull(binding.labelEdit.getText()).toString();
                    if (newName.isEmpty())
                        exitEditMode(app);
                    else if (app.isCustomized() && newName.equals(app.getLabelNew()))
                        exitEditMode(app);
                    else if (!app.isCustomized() && newName.equals(app.getLabelOld()))
                        exitEditMode(app);
                    else
                    {
                        exitEditMode(app);
                        renameApp(app, newName);
                    }
                });
                notifyItemChanged(userApps.indexOf(app));
            }

            private void exitEditMode(UserApp app)
            {
                activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                setOnClickListener(app);
                ((AppListItemBinding)binding).labelEdit.clearFocus();
                app.setBeingEdited(false);
                notifyItemChanged(userApps.indexOf(app));
            }

            private void resetEditMode(UserApp app)
            {
                app.setBeingEdited(false);
                if (!app.isCustomized())
                    exitEditMode(app);
                else
                    resetAppEdit(app);
            }

            private void setOnClickListener(UserApp app)
            {
                itemView.setOnClickListener(v ->
                {

                    Intent intent = packageManager.getLaunchIntentForPackage(app.getPackageName());
                    if (intent != null)
                        itemView.getContext().startActivity(intent,
                                ActivityOptions.makeClipRevealAnimation(itemView,0,0,100, 100).toBundle());
                });
            }

            private void removeOnClickListener()
            {
                itemView.setOnClickListener(v ->{});
            }

        }
    }
}
