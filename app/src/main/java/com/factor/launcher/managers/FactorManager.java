package com.factor.launcher.managers;

import android.app.Activity;
import android.appwidget.AppWidgetHostView;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.util.Log;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityOptionsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import com.factor.launcher.R;
import com.factor.launcher.database.FactorsDatabase;
import com.factor.launcher.databinding.FactorLargeBinding;
import com.factor.launcher.databinding.FactorMediumBinding;
import com.factor.launcher.databinding.FactorSmallBinding;
import com.factor.launcher.models.Factor;
import com.factor.launcher.models.UserApp;
import com.factor.launcher.ui.AnimatedConstraintLayout;
import com.factor.launcher.util.Constants;
import com.factor.launcher.util.Payload;
import com.valkriaine.factor.BouncyRecyclerView;
import eightbitlab.com.blurview.RenderScriptBlur;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public class FactorManager
{
    private final ArrayList<Factor> userFactors = new ArrayList<>();

    private final FactorsDatabase factorsDatabase;

    private final LauncherApps.ShortcutQuery shortcutQuery;

    private final PackageManager packageManager;

    public FactorsAdapter adapter;

    private final Activity activity;

    private ViewGroup background;

    private final LauncherApps launcherApps;

    private final boolean isLiveWallpaper;

    //constructor
    public FactorManager(Activity activity, ViewGroup background, PackageManager pm, LauncherApps launcherApps, LauncherApps.ShortcutQuery shortcutQuery, Boolean isLiveWallpaper)
    {
        this.activity = activity;
        this.background = background;
        this.packageManager = pm;
        this.shortcutQuery = shortcutQuery;
        this.launcherApps = launcherApps;
        this.isLiveWallpaper = isLiveWallpaper;

        adapter = new FactorsAdapter();
        factorsDatabase = Room.databaseBuilder(activity, FactorsDatabase.class, "factor_list").build();
        loadFactors();
    }

    //load factors from sqlite
    private void loadFactors()
    {
        new Thread(()->
        {
            userFactors.addAll(factorsDatabase.factorsDao().getAll());
            for (Factor f: userFactors)
            {
                try
                {
                    if (packageManager.getApplicationInfo(f.getPackageName(), 0).enabled)
                    {
                        f.setIcon(packageManager.getApplicationIcon(f.getPackageName()));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
                            f.setShortcuts(getShortcutsFromFactor(f));
                    }

                }
                catch (Exception e)
                {
                    Log.d("icon", "failed to load icon for " + f.getPackageName() + " " +  e.getMessage());
                    factorsDatabase.factorsDao().delete(f);
                }
            }
            activity.runOnUiThread(adapter::notifyDataSetChanged);
        }).start();
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
            Log.d("add", "Shortcuts:  " + factor.getUserApp().getShortCuts().toString());
            factorsDatabase.factorsDao().insert(factor);
            addFactorBroadcast(userFactors.indexOf(factor));
            activity.runOnUiThread(()-> adapter.notifyItemInserted(factor.getOrder()));
        }).start();
        updateOrders();
    }

    //remove factor from home
    public void removeFromHome(Factor factor)
    {
        new Thread(() ->
        {
            int position = userFactors.indexOf(factor);
            userFactors.remove(factor);
            factorsDatabase.factorsDao().delete(factor);
            updateOrders();
            activity.runOnUiThread(()->adapter.notifyItemRemoved(position));
        }).start();
    }

    //check if the app is added to home
    public boolean isAppPinned(UserApp app)
    {
        for (Factor f : userFactors)
            if (f.getPackageName().equals(app.getPackageName())) return true;

        return false;
    }

    //resize a factor
    private boolean resizeFactor(Factor factor, int size)
    {
        factor.setSize(size);
        return  updateFactor(factor);
    }

    //update factor info after editing
    private boolean updateFactor(Factor factor)
    {
        if (!userFactors.contains(factor))
            return false;

        if (userFactors.contains(factor))
        {
            int position = userFactors.indexOf(factor);
            factor.setOrder(position);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
                factor.setShortcuts(getShortcutsFromFactor(factor));

            new Thread(()->
            {
                factorsDatabase.factorsDao().updateFactorInfo(factor);
                activity.runOnUiThread(() -> adapter.notifyItemChanged(position));
            }).start();
        }
        return true;
    }

    //update factor info after its app has changed
    public void updateFactor(UserApp app)
    {
        ArrayList<Factor> factorsToUpdate = getFactorsByPackage(app);
        Log.d("updateFactor", "size: " + factorsToUpdate.size());
        for (Factor f : factorsToUpdate)
        {
            loadIcon(f);
            f.setLabelOld(app.getLabelOld());
            f.setLabelNew(app.getLabelNew());
            f.setUserApp(app);
            new Thread(()->
            {
                factorsDatabase.factorsDao().updateFactorInfo(f);
                activity.runOnUiThread(() -> adapter.notifyItemChanged(userFactors.indexOf(f)));
            }).start();
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
                factorsDatabase.factorsDao().updateFactorOrder(f.getPackageName(), f.getOrder());
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

    //search for factor given package name
    private Factor getFactorByPackage(String packageName)
    {
        for (Factor f : userFactors)
        {
            if (f.getPackageName().equals(packageName))
                return f;
        }
        return new Factor();
    }

    //retrieve the icon for a given factor
    private void loadIcon(Factor factor)
    {
        try
        {
            if (packageManager.getApplicationInfo(factor.getPackageName(), 0).enabled)
                factor.setIcon(packageManager.getApplicationIcon(factor.getPackageName()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            new Thread(() -> factorsDatabase.factorsDao().delete(factor)).start();
        }
    }

    //send a broadcast after removing a factor to update app list
    private boolean removeFactorBroadcast(Factor factor)
    {
        Intent intent = new Intent();
        intent.setAction(Constants.BROADCAST_ACTION_REMOVE);
        intent.putExtra(Constants.REMOVE_KEY, factor.getPackageName());
        activity.sendBroadcast(intent);
        removeFromHome(factor);
        return true;
    }

    //send a broadcast when a factor has been added to home screen
    private void addFactorBroadcast(int position)
    {
        Intent intent = new Intent();
        intent.setAction(Constants.BROADCAST_ACTION_ADD);
        intent.putExtra(Constants.ADD_KEY, position);
        activity.sendBroadcast(intent);
    }

    //received notification
    public void onReceivedNotification(Intent intent, UserApp app, Payload payload)
    {
        Factor factorToUpdate = getFactorByPackage(intent.getStringExtra(Constants.NOTIFICATION_INTENT_PACKAGE_KEY));
        if (userFactors.contains(factorToUpdate))
        {
            factorToUpdate.setUserApp(app);
            adapter.notifyItemChanged(userFactors.indexOf(factorToUpdate), payload);
        }
    }

    //cleared notification
    public void onClearedNotification(Intent intent, UserApp app, Payload payload)
    {
        Factor factorToUpdate = getFactorByPackage(intent.getStringExtra(Constants.NOTIFICATION_INTENT_PACKAGE_KEY));
        if (userFactors.contains(factorToUpdate))
        {
            factorToUpdate.setUserApp(app);
            adapter.notifyItemChanged(userFactors.indexOf(factorToUpdate), payload);
        }
    }

    public void invalidate()
    {
        this.background = null;
    }


    //find shortcuts related to a factor
    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    public List<ShortcutInfo> getShortcutsFromFactor(Factor factor)
    {

        if (launcherApps == null || !launcherApps.hasShortcutHostPermission())
            return new ArrayList<>(0);

        shortcutQuery.setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC|
                LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST|
                LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED);

        shortcutQuery.setPackage(factor.getPackageName());
        List<ShortcutInfo> shortcuts = launcherApps.getShortcuts(shortcutQuery, Process.myUserHandle());
        if (shortcuts == null || shortcuts.isEmpty())
            return new ArrayList<>(0);
        else
            return shortcuts;
    }

    //launch shortcut
    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    private void startShortCut(ShortcutInfo shortcutInfo)
    {
        launcherApps.startShortcut(shortcutInfo.getPackage(), shortcutInfo.getId(), null, null, Process.myUserHandle());
    }

    //add widget to tiles list
    public void addWidget(AppWidgetHostView appWidgetHostView)
    {

    }

    class FactorsAdapter extends BouncyRecyclerView.Adapter
    {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            int id = 0;

            //determine factor size
            switch (viewType)
            {
                case Factor.Size.small:
                    id = R.layout.factor_small;
                    break;
                case Factor.Size.medium:
                    id = R.layout.factor_medium;
                    break;
                case Factor.Size.large:
                    id = R.layout.factor_large;
                    break;

            }

            View view = LayoutInflater.from(parent.getContext()).inflate(id, parent, false);

            //resize to fit screen
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            int scale = (int)(parent.getWidth() * 0.8 + 0.5f) - parent.getPaddingLeft();



            switch (viewType)
            {
                case Factor.Size.small:
                    layoutParams.width = (int) (scale/2 + 0.5f);
                    layoutParams.height = (int) (scale/2 + 0.5f);
                    break;
                case Factor.Size.medium:
                    layoutParams.height = (int) (scale/2 + 0.5f);
                    layoutParams.width =scale;
                    break;
                case Factor.Size.large:
                    layoutParams.width = scale;
                    layoutParams.height = scale;
                    break;
            }

            view.setLayoutParams(layoutParams);
            activity.registerForContextMenu(view);

            ViewDataBinding binding = DataBindingUtil.bind(view);


            if (isLiveWallpaper)
            {
                if (binding instanceof FactorSmallBinding)
                {
                    ((FactorSmallBinding) binding).trans.setVisibility(View.INVISIBLE);
                    ((FactorSmallBinding) binding).card.setCardBackgroundColor(Color.WHITE);
                }


                if (binding instanceof FactorMediumBinding)
                {
                    ((FactorMediumBinding) binding).trans.setVisibility(View.INVISIBLE);
                    ((FactorMediumBinding) binding).card.setCardBackgroundColor(Color.WHITE);
                }

                if (binding instanceof FactorLargeBinding)
                {
                    ((FactorLargeBinding) binding).trans.setVisibility(View.INVISIBLE);
                    ((FactorLargeBinding) binding).card.setCardBackgroundColor(Color.WHITE);
                }
            }



            view.setOnCreateContextMenuListener((menu, v, menuInfo) ->
            {
                Factor selectedFactor;
                if (binding instanceof FactorLargeBinding)
                    selectedFactor = ((FactorLargeBinding) binding).getFactor();
                else if (binding instanceof FactorMediumBinding)
                    selectedFactor = ((FactorMediumBinding) binding).getFactor();
                else {
                    assert binding instanceof FactorSmallBinding;
                    selectedFactor = ((FactorSmallBinding) binding).getFactor();
                }

                if (!selectedFactor.getPackageName().isEmpty())
                {
                    MenuInflater inflater = activity.getMenuInflater();
                    inflater.inflate(R.menu.factor_item_menu, menu);

                    //remove from home
                    menu.getItem(0).setOnMenuItemClickListener(item -> removeFactorBroadcast(selectedFactor));

                    //resize
                    SubMenu subMenu = menu.getItem(1).getSubMenu();
                    subMenu.getItem(0).setOnMenuItemClickListener(item -> resizeFactor(selectedFactor, Factor.Size.small));
                    subMenu.getItem(1).setOnMenuItemClickListener(item -> resizeFactor(selectedFactor, Factor.Size.medium));
                    subMenu.getItem(2).setOnMenuItemClickListener(item -> resizeFactor(selectedFactor, Factor.Size.large));

                    //info
                    menu.getItem(2).setOnMenuItemClickListener(item ->
                    {
                        view.getContext().startActivity(
                                new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.parse("package:"+selectedFactor.getPackageName())));
                        return true;
                    });

                    //uninstall
                    menu.getItem(3).setOnMenuItemClickListener(item ->
                    {
                        view.getContext().startActivity(new Intent(Intent.ACTION_DELETE, Uri.parse("package:"+selectedFactor.getPackageName()))
                                .putExtra(Intent.EXTRA_RETURN_RESULT, true));
                        return true;
                    });

                    switch (selectedFactor.getSize())
                    {
                        case Factor.Size.small:
                            subMenu.getItem(0).setEnabled(false);
                            break;
                        case Factor.Size.medium:
                            subMenu.getItem(1).setEnabled(false);
                            break;
                        case Factor.Size.large:
                            subMenu.getItem(2).setEnabled(false);
                            break;
                    }
                }

            });


            return new FactorsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
        {
            FactorsViewHolder factorsViewHolder = (FactorsViewHolder)holder;
            factorsViewHolder.bindFactor(userFactors.get(position));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads)
        {
            if (!payloads.isEmpty())
            {
                Log.d("payload", "received");
                ViewDataBinding binding = ((FactorsViewHolder)holder).binding;
                if (!(payloads.get(0) instanceof Payload))
                    onBindViewHolder(holder, position);
                else {
                    assert binding != null;
                    Factor factorToChange = userFactors.get(position);
                    if (userFactors.get(position).getSize() == Factor.Size.small)
                    {
                        FactorSmallBinding tileBinding = (FactorSmallBinding)binding;

                        if (factorToChange.retrieveNotificationCountInNumber() > 0)
                            tileBinding.notificationCount.setText(factorToChange.retrieveNotificationCount());

                        tileBinding.notificationCount.setVisibility(factorToChange.visibilityNotificationCount());
                    }
                    else if (userFactors.get(position).getSize() == Factor.Size.medium)
                    {
                        FactorMediumBinding tileBinding = (FactorMediumBinding) binding;

                        if (factorToChange.retrieveNotificationCountInNumber() > 0)
                            tileBinding.notificationCount.setText(factorToChange.retrieveNotificationCount());

                        tileBinding.notificationCount.setVisibility(factorToChange.visibilityNotificationCount());
                        tileBinding.notificationTitle.setText(factorToChange.getUserApp().getNotificationTitle());
                        tileBinding.notificationContent.setText(factorToChange.getUserApp().getNotificationText());

                    }
                    else if (userFactors.get(position).getSize() == Factor.Size.large)
                    {
                        FactorLargeBinding tileBinding = (FactorLargeBinding) binding;

                        if (factorToChange.retrieveNotificationCountInNumber() > 0)
                            tileBinding.notificationCount.setText(factorToChange.retrieveNotificationCount());

                        tileBinding.notificationCount.setVisibility(factorToChange.visibilityNotificationCount());
                        tileBinding.notificationTitle.setText(factorToChange.getUserApp().getNotificationTitle());
                        tileBinding.notificationContent.setText(factorToChange.getUserApp().getNotificationText());
                    }
                }
            }
            else
                onBindViewHolder(holder, position);


        }

        @Override
        public int getItemCount()
        {
            return userFactors.size();
        }

        @Override
        public void onItemMoved(int fromPosition, int toPosition)
        {
            activity.closeContextMenu();
            Factor f = userFactors.remove(fromPosition);
            userFactors.add(toPosition, f);
            notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public int getItemViewType(int position)
        {
            Factor f = userFactors.get(position);
            return f.getSize();
        }

        @Override
        public void onItemReleased(@Nullable RecyclerView.ViewHolder viewHolder)
        {
            updateOrders();
            assert viewHolder != null;
            ((AnimatedConstraintLayout)(viewHolder.itemView)).animateToNormalState();
            viewHolder.itemView.setAlpha(1f);
        }

        @Override
        public void onItemSelected(@Nullable RecyclerView.ViewHolder viewHolder)
        {
            assert viewHolder != null;
            ((AnimatedConstraintLayout)(viewHolder.itemView)).animateToSelectedState();
            viewHolder.itemView.setAlpha(0.9f);
        }

        @Override
        public void onItemSwipedToEnd(@Nullable RecyclerView.ViewHolder viewHolder, int position)
        {

        }

        @Override
        public void onItemSwipedToStart(@Nullable RecyclerView.ViewHolder viewHolder, int position)
        {

        }

        class FactorsViewHolder extends RecyclerView.ViewHolder
        {
            public final ViewDataBinding binding;
            private int size = 0;

            public FactorsViewHolder(@NonNull View itemView)
            {
                super(itemView);
                binding = DataBindingUtil.bind(itemView);
            }

            public void bindFactor(Factor factor)
            {
                //determine layout based on size
                size = factor.getSize();

                if (size == Factor.Size.small)
                {
                    ((FactorSmallBinding)binding).setFactor(factor);
                    ((FactorSmallBinding) binding).tileLabel.setText(factor.getLabelNew());
                    try
                    {
                        ((FactorSmallBinding)binding).tileIcon.setImageDrawable(factor.getIcon());
                    }
                    catch (kotlin.UninitializedPropertyAccessException ex)
                    {
                        loadIcon(factor);
                        ((FactorSmallBinding)binding).tileIcon.setImageDrawable(factor.getIcon());
                    }

                    ((FactorSmallBinding)binding).notificationCount.setVisibility(factor.visibilityNotificationCount());

                    if (factor.retrieveNotificationCountInNumber() > 0)
                        ((FactorSmallBinding)binding).notificationCount.setText(factor.retrieveNotificationCount());

                    if (!isLiveWallpaper)
                        ((FactorSmallBinding) binding).trans
                            .setupWith(background)
                            .setBlurAlgorithm(new RenderScriptBlur(activity))
                            .setBlurRadius(10F)
                            .setBlurAutoUpdate(false)
                            .setHasFixedTransformationMatrix(false);
                }
                else if (size == Factor.Size.medium)
                {

                    ((FactorMediumBinding)binding).setFactor(factor);
                    ((FactorMediumBinding) binding).tileLabel.setText(factor.getLabelNew());

                    try
                    {
                        ((FactorMediumBinding)binding).tileIcon.setImageDrawable(factor.getIcon());
                    }
                    catch (kotlin.UninitializedPropertyAccessException ex)
                    {
                        loadIcon(factor);
                        ((FactorMediumBinding)binding).tileIcon.setImageDrawable(factor.getIcon());
                    }

                    ((FactorMediumBinding)binding).notificationTitle.setText(factor.getUserApp().getNotificationTitle());
                    ((FactorMediumBinding)binding).notificationContent.setText(factor.getUserApp().getNotificationText());

                    ((FactorMediumBinding)binding).notificationCount.setVisibility(factor.visibilityNotificationCount());

                    if (factor.retrieveNotificationCountInNumber() > 0)
                        ((FactorMediumBinding)binding).notificationCount.setText(factor.retrieveNotificationCount());

                    if (!isLiveWallpaper)
                        ((FactorMediumBinding) binding).trans
                            .setupWith(background)
                            .setBlurAlgorithm(new RenderScriptBlur(activity))
                            .setBlurRadius(10F)
                            .setBlurAutoUpdate(false)
                            .setHasFixedTransformationMatrix(false);
                }
                else if (size == Factor.Size.large)
                {

                    ((FactorLargeBinding)binding).setFactor(factor);
                    ((FactorLargeBinding)binding).tileLabel.setText(factor.getLabelNew());

                    try
                    {
                        ((FactorLargeBinding)binding).tileIcon.setImageDrawable(factor.getIcon());
                    }
                    catch (kotlin.UninitializedPropertyAccessException ex)
                    {
                        loadIcon(factor);
                        ((FactorLargeBinding)binding).tileIcon.setImageDrawable(factor.getIcon());
                    }

                    ((FactorLargeBinding)binding).notificationCount.setText(factor.retrieveNotificationCount());
                    ((FactorLargeBinding)binding).notificationTitle.setText(factor.getUserApp().getNotificationTitle());
                    ((FactorLargeBinding)binding).notificationContent.setText(factor.getUserApp().getNotificationText());

                    ((FactorLargeBinding)binding).notificationCount.setVisibility(factor.getUserApp().visibilityNotificationCount());

                    if (factor.getUserApp().hasShortcuts())
                    {
                        ((FactorLargeBinding)binding).shortcutAvailability.setVisibility(View.INVISIBLE);
                        List<ShortcutInfo> shortcuts = getShortcutsFromFactor(factor);
                        if (shortcuts.get(0) != null)
                        {
                            ShortcutInfo shortcut1 = shortcuts.get(0);
                            ((FactorLargeBinding)binding).shortcut1.setVisibility(View.VISIBLE);
                            Drawable icon1 = launcherApps.getShortcutIconDrawable(shortcut1, itemView.getContext().getResources().getDisplayMetrics().densityDpi);
                            ((FactorLargeBinding)binding).shortcut1Icon.setImageDrawable(icon1);
                            ((FactorLargeBinding)binding).shortcut1Label.setText(shortcut1.getShortLabel());

                            ((FactorLargeBinding)binding).shortcut1.setOnClickListener(view -> startShortCut(shortcut1));
                        }
                        else
                            ((FactorLargeBinding)binding).shortcut1.setVisibility(View.INVISIBLE);


                        if (shortcuts.size() > 1 && shortcuts.get(1) != null)
                        {
                            ShortcutInfo shortcut2 = shortcuts.get(1);
                            ((FactorLargeBinding)binding).shortcut2.setVisibility(View.VISIBLE);
                            Drawable icon2 = launcherApps.getShortcutIconDrawable(shortcut2, itemView.getContext().getResources().getDisplayMetrics().densityDpi);
                            ((FactorLargeBinding)binding).shortcut2Icon.setImageDrawable(icon2);
                            ((FactorLargeBinding)binding).shortcut2Label.setText(shortcut2.getShortLabel());

                            ((FactorLargeBinding)binding).shortcut2.setOnClickListener(view -> startShortCut(shortcut2));
                        }
                        else
                            ((FactorLargeBinding)binding).shortcut2.setVisibility(View.INVISIBLE);

                        if (shortcuts.size() > 2 && shortcuts.get(2) != null)
                        {
                            ShortcutInfo shortcut3 = shortcuts.get(2);
                            ((FactorLargeBinding)binding).shortcut3.setVisibility(View.VISIBLE);
                            Drawable icon3 = launcherApps.getShortcutIconDrawable(shortcut3, itemView.getContext().getResources().getDisplayMetrics().densityDpi);
                            ((FactorLargeBinding)binding).shortcut3Icon.setImageDrawable(icon3);
                            ((FactorLargeBinding)binding).shortcut3Label.setText(shortcut3.getShortLabel());

                            ((FactorLargeBinding)binding).shortcut3.setOnClickListener(view -> startShortCut(shortcut3));
                        }
                        else
                            ((FactorLargeBinding)binding).shortcut3.setVisibility(View.INVISIBLE);
                    }
                    else
                    {
                        ((FactorLargeBinding)binding).shortcut1.setVisibility(View.INVISIBLE);
                        ((FactorLargeBinding)binding).shortcut2.setVisibility(View.INVISIBLE);
                        ((FactorLargeBinding)binding).shortcut3.setVisibility(View.INVISIBLE);
                        ((FactorLargeBinding)binding).shortcutAvailability.setVisibility(View.VISIBLE);
                    }


                    if (!isLiveWallpaper)
                        ((FactorLargeBinding)binding).trans
                            .setupWith(background)
                            .setBlurAlgorithm(new RenderScriptBlur(activity))
                            .setBlurRadius(10F)
                            .setBlurAutoUpdate(false)
                            .setHasFixedTransformationMatrix(false);
                }

                //todo: customize activity transition animation
                itemView.setOnClickListener(v -> {
                    Intent intent = packageManager.getLaunchIntentForPackage(factor.getPackageName());
                    if (intent != null)
                        itemView.getContext().startActivity(intent,
                                ActivityOptionsCompat.makeClipRevealAnimation(itemView, 0,0,itemView.getWidth(), itemView.getHeight()).toBundle());
                });
            }

            //return the factor of this view holder
            public Factor getFactor()
            {
                if (size == Factor.Size.small)
                {
                    return ((FactorSmallBinding)binding).getFactor();
                }
                else if (size == Factor.Size.medium)
                {
                    return ((FactorMediumBinding)binding).getFactor();
                }
                else if (size == Factor.Size.large)
                {
                    return ((FactorLargeBinding)binding).getFactor();
                }
                else
                    return new Factor();
            }

        }
    }

}
