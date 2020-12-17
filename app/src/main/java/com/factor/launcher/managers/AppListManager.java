package com.factor.launcher.managers;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.*;
import android.net.Uri;
import android.os.Process;
import android.util.Log;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import com.factor.launcher.R;
import com.factor.launcher.database.AppListDatabase;
import com.factor.launcher.databinding.AppListItemBinding;
import com.factor.launcher.models.NotificationHolder;
import com.factor.launcher.models.UserApp;
import com.factor.launcher.util.Constants;
import com.factor.launcher.util.Payload;


import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static com.factor.launcher.util.Constants.PACKAGE_NAME;

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

    private final LauncherApps.ShortcutQuery shortcutQuery = new LauncherApps.ShortcutQuery();

    private SharedPreferences.Editor editor;

    private final FactorManager factorManager;

    public AppListAdapter adapter;

    //constructor
    public AppListManager(Activity activity, ViewGroup background, Boolean isLiveWallpaper)
    {
        this.activity = activity;
        packageManager = activity.getPackageManager();
        launcherApps = (LauncherApps) activity.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        this.adapter = new AppListAdapter();
        this.factorManager = new FactorManager(activity, background, packageManager, launcherApps, shortcutQuery, isLiveWallpaper);
        appListDatabase = Room.databaseBuilder(activity, AppListDatabase.class, "app_drawer_list").build();
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

    //return factor manager
    public FactorManager getFactorManager()
    {
        return this.factorManager;
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
                                    app.setShortCuts(getShortcutsFromApp(app));
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
        else
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
                        if (!r.activityInfo.packageName.equals(Constants.PACKAGE_NAME) && packageManager.getApplicationInfo(r.activityInfo.packageName, 0).enabled)
                        {
                            UserApp app = new UserApp();
                            app.setLabelOld((String) r.loadLabel(packageManager));
                            app.setLabelNew(app.getLabelOld());
                            app.setPackageName(r.activityInfo.packageName);
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
        userApp.setHidden(true);
        new Thread(() ->
        {
            appListDatabase.appListDao().updateAppInfo(userApp);
            activity.runOnUiThread(() -> adapter.notifyItemChanged(userApps.indexOf(userApp)));
        }).start();

        return userApps.contains(userApp);
    }

    //set app to not hidden
    private boolean showApp(UserApp userApp)
    {
        userApp.setHidden(false);
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

    //find app object given package name
    private UserApp findAppByPackage(String packageName)
    {
        UserApp app = new UserApp();
        for (UserApp a : userApps)
        {
            if (a.getPackageName().equals(packageName))
                app = a;
        }

        return app;
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
        if (doesPackageExist(app))
        {
            new Thread(() ->
            {
                try
                {
                    ApplicationInfo info = packageManager.getApplicationInfo(app.getPackageName(), 0);
                    app.setIcon(packageManager.getApplicationIcon(app.getPackageName()));
                    app.setLabelOld((String) packageManager.getApplicationLabel(info));
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

    //update app info in database
    public void updateApp(UserApp app)
    {
        UserApp appToUpdate = findAppByPackage(app.getPackageName());
        if (!appToUpdate.getLabelNew().equals(app.getLabelNew()) && !appToUpdate.isCustomized())
            updateAppReorder(app);
        else
            updateAppNoReorder(app);
    }

    //called when receiving PACKAGE_ADDED broadcast with EXTRA_REPLACING set to true
    private void updateAppNoReorder(UserApp app)
    {
        UserApp appToUpdate;
        try {
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
                            userApps.get(position).setShortCuts(getShortcutsFromApp(app));
                            appListDatabase.appListDao().updateAppInfo(appToUpdate);
                            userApps.sort(first_letter);

                            activity.runOnUiThread(() -> adapter.notifyItemChanged(position));
                        }
                        catch (PackageManager.NameNotFoundException e)
                        {
                            e.printStackTrace();
                        }
                    }).start();

                    if (appToUpdate.isPinned())
                    {
                        factorManager.updateFactor(appToUpdate);
                    }

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
            Log.d(TAG, e.getMessage());
        }
    }

    //same as updateAppNoReorder, but notifyDatasetChanged because the app name has changed
    private void updateAppReorder(UserApp app)
    {
        UserApp appToUpdate;
        try {
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
                            userApps.get(position).setShortCuts(getShortcutsFromApp(app));
                            appListDatabase.appListDao().updateAppInfo(appToUpdate);
                            userApps.sort(first_letter);

                            int newPosition = userApps.indexOf(app);
                            activity.runOnUiThread(() ->
                            {
                                adapter.notifyItemRemoved(position);
                                adapter.notifyItemInserted(newPosition);

                                if (isAfterRename)
                                    renameBroadCast(newPosition);

                                isAfterRename = false;
                            });
                        }
                        catch (PackageManager.NameNotFoundException e)
                        {
                            e.printStackTrace();
                        }
                    }).start();

                    if (appToUpdate.isPinned())
                    {
                        factorManager.updateFactor(appToUpdate);
                    }

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
            Log.d(TAG, e.getMessage());
        }
    }

    //remove from home
    public void unPin(String packageName)
    {
        UserApp appToUnPin = new UserApp();
        for (UserApp app : userApps)
        {
            if (app.getPackageName().equals(packageName))
            {
                appToUnPin = app;
                break;
            }
        }
        if (!appToUnPin.getPackageName().isEmpty())
            changePin(appToUnPin);
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
        if (!userApps.contains(app))
            return;

        app.setCustomized(true);
        app.setLabelNew(newLabel);
        updateAppReorder(app);
        isAfterRename = true;
    }

    //reset app's name back to original label
    public void resetAppEdit(UserApp app)
    {
        if (!userApps.contains(app)) return;
        app.setCustomized(false);
        app.setLabelNew(app.getLabelOld());
        updateAppReorder(app);
        isAfterRename = true;
    }

    //change display mode, return a new adapter
    public AppListAdapter setDisplayHidden(boolean displayHidden)
    {
        this.displayHidden = displayHidden;
        this.adapter = new AppListAdapter();
        return adapter;
    }

    //retrieve list of app shortcuts
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
        launcherApps.startShortcut(shortcutInfo.getPackage(), shortcutInfo.getId(), null, null, Process.myUserHandle());
    }

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

    public UserApp getUserApp(int position)
    {
        UserApp appToFind = userApps.get(position);
        int newPosition = 0;
        queryApps.clear();
        if (!displayHidden)
        {
            for (UserApp app : userApps)
            {
                if (!app.isHidden())
                    queryApps.add(app);
            }
            if (!appToFind.isHidden())
                newPosition = queryApps.indexOf(appToFind);
        }
        else
        {
            for (UserApp app : userApps)
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


    //adapter for app drawer
    public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppListViewHolder>
    {
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
                ((AppListItemBinding)holder.binding).labelEdit.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus)
                    activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                else
                    activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
            });


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
                    appBinding.icon.setImageDrawable(app.getIcon());

                    appBinding.label.setVisibility(app.visibilityLabel());
                    appBinding.labelEdit.setVisibility(app.visibilityEditing());
                    appBinding.editButtonGroup.setVisibility(app.visibilityEditing());
                    appBinding.notificationCount.setVisibility(app.visibilityNotificationCount());

                    if (!app.isBeingEdited())
                    {
                        setOnClickListener(app);
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

                    itemView.setOnCreateContextMenuListener((menu, v, menuInfo) ->
                    {
                        MenuInflater inflater = activity.getMenuInflater();
                        inflater.inflate(R.menu.app_list_item_menu, menu);
                        if (app.isPinned())
                            menu.getItem(0).setEnabled(false);

                        //add to home & remove from home
                        menu.getItem(0).setOnMenuItemClickListener(item -> changePin(app));

                        //edit
                        SubMenu sub = menu.getItem(1).getSubMenu();

                        //rename
                        sub.getItem(0).setOnMenuItemClickListener(item ->
                        {
                            enterEditMode(appBinding);
                            return true;
                        });
                        //hide
                        MenuItem hide = sub.getItem(1);
                        if (app.isHidden())
                            hide.setTitle("Show");
                        else hide.setTitle("Hide");
                        hide.setOnMenuItemClickListener(item -> !app.isHidden() ? hideApp(app) : showApp(app));
                        //info
                        menu.getItem(2).setOnMenuItemClickListener(item ->
                        {
                            activity.startActivity(
                                    new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.parse("package:"+app.getPackageName())));
                            return true;
                        });

                        //uninstall
                        menu.getItem(3).setOnMenuItemClickListener(item ->
                        {
                            activity.startActivity(new Intent(Intent.ACTION_DELETE, Uri.parse("package:"+app.getPackageName()))
                                    .putExtra(Intent.EXTRA_RETURN_RESULT, true));
                            return true;
                        });
                    });
                }
            }


            private void enterEditMode(AppListItemBinding binding)
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
                        activity.startActivity(intent,
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
