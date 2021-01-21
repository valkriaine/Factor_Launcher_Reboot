package com.factor.launcher.adapters;

import android.app.Activity;
import android.content.Intent;
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
import com.factor.bouncy.BouncyRecyclerView;
import com.factor.launcher.R;
import com.factor.launcher.databinding.FactorLargeBinding;
import com.factor.launcher.databinding.FactorMediumBinding;
import com.factor.launcher.databinding.FactorSmallBinding;
import com.factor.launcher.view_models.FactorManager;
import com.factor.launcher.models.AppSettings;
import com.factor.launcher.models.Factor;
import com.factor.launcher.models.NotificationHolder;
import com.factor.launcher.models.UserApp;
import com.factor.launcher.ui.AnimatedConstraintLayout;
import com.factor.launcher.util.Constants;
import com.factor.launcher.util.Payload;
import com.factor.launcher.util.Util;
import eightbitlab.com.blurview.RenderScriptBlur;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FactorsAdapter extends BouncyRecyclerView.Adapter<FactorsAdapter.FactorsViewHolder>
{
    private AppSettings appSettings;

    public Activity activity;

    private final boolean isLiveWallpaper;

    private ArrayList<Factor> userFactors;

    private ViewGroup background;

    private FactorManager factorManager;


    public FactorsAdapter(FactorManager factorManager, AppSettings appSettings, Activity activity, boolean isLiveWallpaper, ArrayList<Factor> userFactors, ViewGroup background)
    {
        this.appSettings = appSettings;
        this.factorManager = factorManager;
        this.activity = activity;
        this.isLiveWallpaper = isLiveWallpaper;
        this.userFactors = userFactors;
        this.background = background;
    }

    public void invalidate()
    {
        this.background = null;
        this.activity = null;
        this.factorManager = null;
        this.userFactors = null;
        this.appSettings = null;
    }


    //send a broadcast after removing a factor to update app list
    private boolean removeFactorBroadcast(Factor factor)
    {
        Intent intent = new Intent();
        intent.setAction(Constants.BROADCAST_ACTION_REMOVE);
        intent.putExtra(Constants.REMOVE_KEY, factor.getPackageName());
        activity.getApplicationContext().sendBroadcast(intent);
        factorManager.removeFromHome(factor);
        return true;
    }


    @NonNull
    @Override
    public FactorsAdapter.FactorsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
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
        int scale = (int)(parent.getWidth() * appSettings.getTileListScale()) - parent.getPaddingLeft();



        switch (viewType)
        {
            case Factor.Size.small:
                layoutParams.width = scale/2;
                layoutParams.height = scale/2;
                break;
            case Factor.Size.medium:
                layoutParams.height = scale/2;
                layoutParams.width = scale;
                break;
            case Factor.Size.large:
                layoutParams.width = scale;
                layoutParams.height = scale;
                break;
        }

        int padding = (int) Util.INSTANCE.dpToPx(appSettings.getTileMargin(), parent.getContext());
        view.setPadding(padding, padding, padding, padding);
        view.setLayoutParams(layoutParams);
        activity.registerForContextMenu(view);

        ViewDataBinding binding = DataBindingUtil.bind(view);

        //check blur enabled
        if (isLiveWallpaper || !appSettings.isBlurred())
        {
            if (binding instanceof FactorSmallBinding)
            {
                ((FactorSmallBinding) binding).trans.setVisibility(View.INVISIBLE);
                ((FactorSmallBinding) binding).card.setCardBackgroundColor(Color.parseColor("#" + appSettings.getTileThemeColor()));
            }
            if (binding instanceof FactorMediumBinding)
            {
                ((FactorMediumBinding) binding).trans.setVisibility(View.INVISIBLE);
                ((FactorMediumBinding) binding).card.setCardBackgroundColor(Color.parseColor("#" + appSettings.getTileThemeColor()));
            }
            if (binding instanceof FactorLargeBinding)
            {
                ((FactorLargeBinding) binding).trans.setVisibility(View.INVISIBLE);
                ((FactorLargeBinding) binding).card.setCardBackgroundColor(Color.parseColor("#" + appSettings.getTileThemeColor()));
            }
        }
        else
        {
            if (binding instanceof FactorSmallBinding)
            {
                ((FactorSmallBinding) binding).trans.setVisibility(View.VISIBLE);
                ((FactorSmallBinding) binding).card.setCardBackgroundColor(Color.TRANSPARENT);
            }
            if (binding instanceof FactorMediumBinding)
            {
                ((FactorMediumBinding) binding).trans.setVisibility(View.VISIBLE);
                ((FactorMediumBinding) binding).card.setCardBackgroundColor(Color.TRANSPARENT);
            }
            if (binding instanceof FactorLargeBinding)
            {
                ((FactorLargeBinding) binding).trans.setVisibility(View.VISIBLE);
                ((FactorLargeBinding) binding).card.setCardBackgroundColor(Color.TRANSPARENT);
            }
        }


        if (binding instanceof FactorSmallBinding)
        {
            if (appSettings.getShowShadowAroundIcon())
                ((FactorSmallBinding) binding).tileIcon.setElevationDp(50);
            else
                ((FactorSmallBinding) binding).tileIcon.setElevationDp(0);

            ((FactorSmallBinding) binding).card.setRadius(Util.INSTANCE.dpToPx(appSettings.getCornerRadius(), parent.getContext()));
            ((FactorSmallBinding) binding).card.setRadius(Util.INSTANCE.dpToPx(appSettings.getCornerRadius(), parent.getContext()));
        }

        if (binding instanceof FactorMediumBinding)
        {
            if (appSettings.getShowShadowAroundIcon())
                ((FactorMediumBinding) binding).tileIcon.setElevationDp(50);
            else
                ((FactorMediumBinding) binding).tileIcon.setElevationDp(0);

            ((FactorMediumBinding) binding).card.setRadius(Util.INSTANCE.dpToPx(appSettings.getCornerRadius(), parent.getContext()));
        }

        if (binding instanceof FactorLargeBinding)
        {
            if (appSettings.getShowShadowAroundIcon())
            {
                ((FactorLargeBinding) binding).tileIcon.setElevationDp(50);
                ((FactorLargeBinding) binding).shortcut1Icon.setElevation(Util.INSTANCE.dpToPx(10, parent.getContext()));
                ((FactorLargeBinding) binding).shortcut2Icon.setElevation(Util.INSTANCE.dpToPx(10, parent.getContext()));
                ((FactorLargeBinding) binding).shortcut3Icon.setElevation(Util.INSTANCE.dpToPx(10, parent.getContext()));
            }

            else
            {
                ((FactorLargeBinding) binding).tileIcon.setElevationDp(0);
                ((FactorLargeBinding) binding).shortcut1Card.setElevation(0);
                ((FactorLargeBinding) binding).shortcut2Card.setElevation(0);
                ((FactorLargeBinding) binding).shortcut3Card.setElevation(0);
            }

            ((FactorLargeBinding) binding).card.setRadius(Util.INSTANCE.dpToPx(appSettings.getCornerRadius(), parent.getContext()));
        }

        //create context menu
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
                subMenu.getItem(0).setOnMenuItemClickListener(item -> factorManager.resizeFactor(selectedFactor, Factor.Size.small));
                subMenu.getItem(1).setOnMenuItemClickListener(item -> factorManager.resizeFactor(selectedFactor, Factor.Size.medium));
                subMenu.getItem(2).setOnMenuItemClickListener(item -> factorManager.resizeFactor(selectedFactor, Factor.Size.large));

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


        return new FactorsAdapter.FactorsViewHolder(view, appSettings, factorManager, isLiveWallpaper, background);
    }

    @Override
    public void onBindViewHolder(@NonNull FactorsAdapter.FactorsViewHolder holder, int position)
    {
        holder.bindFactor(userFactors.get(position));
    }

    @Override
    public void onBindViewHolder(@NonNull FactorsAdapter.FactorsViewHolder holder, int position, @NonNull List<Object> payloads)
    {
        if (!payloads.isEmpty())
        {
            Log.d("payload", "received");
            ViewDataBinding binding = holder.binding;
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
        factorManager.updateOrders();
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


    //send a broadcast when a factor has been added to home screen
    public void addFactorBroadcast(int position)
    {
        Intent intent = new Intent();
        intent.setAction(Constants.BROADCAST_ACTION_ADD);
        intent.putExtra(Constants.ADD_KEY, position);
        activity.getApplicationContext().sendBroadcast(intent);
    }


    //received notification
    public void onReceivedNotification(Intent intent, UserApp app, Payload payload)
    {
        Factor factorToUpdate = getFactorByPackage(intent.getStringExtra(Constants.NOTIFICATION_INTENT_PACKAGE_KEY));
        if (userFactors.contains(factorToUpdate))
        {
            factorToUpdate.setUserApp(app);
            notifyItemChanged(userFactors.indexOf(factorToUpdate), payload);
        }
    }

    //cleared notification
    public void onClearedNotification(Intent intent, UserApp app, Payload payload)
    {
        Factor factorToUpdate = getFactorByPackage(intent.getStringExtra(Constants.NOTIFICATION_INTENT_PACKAGE_KEY));
        if (userFactors.contains(factorToUpdate))
        {
            factorToUpdate.setUserApp(app);
            notifyItemChanged(userFactors.indexOf(factorToUpdate), payload);
        }
    }

    //update UI given the app
    public void clearNotification(UserApp app)
    {
        Factor factor = getFactorByPackage(app.getPackageName());
        if (!factor.getPackageName().isEmpty())
            notifyItemChanged(userFactors.indexOf(factor),
                    new Payload(new NotificationHolder(0, "", "")));
    }


    //search for factor given package name
    private Factor getFactorByPackage(String packageName)
    {
        ArrayList<Factor> copyFactors = new ArrayList<>(userFactors);
        for (Factor f : copyFactors)
        {
            if (f.getPackageName().equals(packageName))
                return f;
        }
        return new Factor();
    }



    //Tile ViewHolder
    protected static class FactorsViewHolder extends RecyclerView.ViewHolder
    {
        public final ViewDataBinding binding;

        private int size = 0;

        private final AppSettings appSettings;

        private final FactorManager factorManager;

        private final boolean isLiveWallpaper;

        private final ViewGroup background;


        public FactorsViewHolder(@NonNull View itemView, AppSettings appSettings, FactorManager factorManager, boolean isLiveWallpaper, ViewGroup background)
        {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            this.appSettings = appSettings;
            this.factorManager = factorManager;
            this.isLiveWallpaper = isLiveWallpaper;
            this.background = background;
        }

        public void bindFactor(Factor factor)
        {
            //determine layout based on size
            size = factor.getSize();

            switch (size)
            {
                case Factor.Size.small:
                    ((FactorSmallBinding) binding).setFactor(factor);
                    ((FactorSmallBinding) binding).tileLabel.setText(factor.getLabelNew());

                    if (appSettings.isDarkText())
                        ((FactorSmallBinding) binding).tileLabel.setTextColor(Color.BLACK);
                    else
                        ((FactorSmallBinding) binding).tileLabel.setTextColor(Color.WHITE);

                    try {
                        ((FactorSmallBinding) binding).tileIcon.setImageDrawable(factor.getIcon());
                    } catch (kotlin.UninitializedPropertyAccessException ex) {
                        factorManager.loadIcon(factor);
                        ((FactorSmallBinding) binding).tileIcon.setImageDrawable(factor.getIcon());
                    }

                    ((FactorSmallBinding) binding).notificationCount.setVisibility(factor.visibilityNotificationCount());

                    if (factor.retrieveNotificationCountInNumber() > 0)
                        ((FactorSmallBinding) binding).notificationCount.setText(factor.retrieveNotificationCount());

                    if (!isLiveWallpaper) {
                        ((FactorSmallBinding) binding).trans
                                .setupWith(background)
                                .setOverlayColor(Color.parseColor("#" + appSettings.getTileThemeColor()))
                                .setBlurAlgorithm(new RenderScriptBlur(itemView.getContext()))
                                .setBlurRadius(appSettings.getBlurRadius())
                                .setBlurAutoUpdate(false)
                                .setHasFixedTransformationMatrix(false);
                    }
                    break;


                case Factor.Size.medium:

                    ((FactorMediumBinding) binding).setFactor(factor);
                    ((FactorMediumBinding) binding).tileLabel.setText(factor.getLabelNew());

                    if (appSettings.isDarkText()) {
                        ((FactorMediumBinding) binding).tileLabel.setTextColor(Color.BLACK);
                        ((FactorMediumBinding) binding).notificationTitle.setTextColor(Color.BLACK);
                        ((FactorMediumBinding) binding).notificationContent.setTextColor(Color.BLACK);
                    } else {
                        ((FactorMediumBinding) binding).tileLabel.setTextColor(Color.WHITE);
                        ((FactorMediumBinding) binding).notificationTitle.setTextColor(Color.WHITE);
                        ((FactorMediumBinding) binding).notificationContent.setTextColor(Color.WHITE);
                    }


                    try {
                        ((FactorMediumBinding) binding).tileIcon.setImageDrawable(factor.getIcon());
                    } catch (kotlin.UninitializedPropertyAccessException ex) {
                        factorManager.loadIcon(factor);
                        ((FactorMediumBinding) binding).tileIcon.setImageDrawable(factor.getIcon());
                    }

                    ((FactorMediumBinding) binding).notificationTitle.setText(factor.getUserApp().getNotificationTitle());
                    ((FactorMediumBinding) binding).notificationContent.setText(factor.getUserApp().getNotificationText());

                    ((FactorMediumBinding) binding).notificationCount.setVisibility(factor.visibilityNotificationCount());

                    if (factor.retrieveNotificationCountInNumber() > 0)
                        ((FactorMediumBinding) binding).notificationCount.setText(factor.retrieveNotificationCount());

                    if (!isLiveWallpaper)
                        ((FactorMediumBinding) binding).trans
                                .setupWith(background)
                                .setOverlayColor(Color.parseColor("#" + appSettings.getTileThemeColor()))
                                .setBlurAlgorithm(new RenderScriptBlur(itemView.getContext()))
                                .setBlurRadius(appSettings.getBlurRadius())
                                .setBlurAutoUpdate(false)
                                .setHasFixedTransformationMatrix(false);
                    break;




                case Factor.Size.large:

                    ((FactorLargeBinding) binding).setFactor(factor);
                    ((FactorLargeBinding) binding).tileLabel.setText(factor.getLabelNew());


                    if (appSettings.isDarkText()) {
                        ((FactorLargeBinding) binding).tileLabel.setTextColor(Color.BLACK);
                        ((FactorLargeBinding) binding).shortcut1Label.setTextColor(Color.BLACK);
                        ((FactorLargeBinding) binding).shortcut2Label.setTextColor(Color.BLACK);
                        ((FactorLargeBinding) binding).shortcut3Label.setTextColor(Color.BLACK);
                        ((FactorLargeBinding) binding).notificationContent.setTextColor(Color.BLACK);
                        ((FactorLargeBinding) binding).notificationTitle.setTextColor(Color.BLACK);

                        ((FactorLargeBinding) binding).divider.setBackgroundColor(Color.BLACK);
                    } else {
                        ((FactorLargeBinding) binding).tileLabel.setTextColor(Color.WHITE);
                        ((FactorLargeBinding) binding).shortcut1Label.setTextColor(Color.WHITE);
                        ((FactorLargeBinding) binding).shortcut2Label.setTextColor(Color.WHITE);
                        ((FactorLargeBinding) binding).shortcut3Label.setTextColor(Color.WHITE);
                        ((FactorLargeBinding) binding).notificationContent.setTextColor(Color.WHITE);
                        ((FactorLargeBinding) binding).notificationTitle.setTextColor(Color.WHITE);

                        ((FactorLargeBinding) binding).divider.setBackgroundColor(Color.WHITE);
                    }


                    try {
                        ((FactorLargeBinding) binding).tileIcon.setImageDrawable(factor.getIcon());
                    } catch (kotlin.UninitializedPropertyAccessException ex) {
                        factorManager.loadIcon(factor);
                        ((FactorLargeBinding) binding).tileIcon.setImageDrawable(factor.getIcon());
                    }

                    ((FactorLargeBinding) binding).notificationCount.setText(factor.retrieveNotificationCount());
                    ((FactorLargeBinding) binding).notificationTitle.setText(factor.getUserApp().getNotificationTitle());
                    ((FactorLargeBinding) binding).notificationContent.setText(factor.getUserApp().getNotificationText());
                    ((FactorLargeBinding) binding).notificationCount.setVisibility(factor.getUserApp().visibilityNotificationCount());

                    if (factor.retrieveNotificationCountInNumber() > 0)
                        ((FactorLargeBinding) binding).notificationCount.setText(factor.retrieveNotificationCount());

                    if (factor.getUserApp().hasShortcuts()) {
                        ((FactorLargeBinding) binding).shortcutAvailability.setVisibility(View.INVISIBLE);
                        List<ShortcutInfo> shortcuts = factorManager.getShortcutsFromFactor(factor);
                        if (shortcuts.get(0) != null) {
                            ShortcutInfo shortcut1 = shortcuts.get(0);
                            ((FactorLargeBinding) binding).shortcut1.setVisibility(View.VISIBLE);
                            Drawable icon1 = factorManager.launcherApps.getShortcutIconDrawable(shortcut1, itemView.getContext().getResources().getDisplayMetrics().densityDpi);
                            ((FactorLargeBinding) binding).shortcut1Icon.setImageDrawable(icon1);
                            ((FactorLargeBinding) binding).shortcut1Label.setText(shortcut1.getShortLabel());

                            ((FactorLargeBinding) binding).shortcut1.setOnClickListener(view -> startShortCut(shortcut1));
                        } else
                            ((FactorLargeBinding) binding).shortcut1.setVisibility(View.INVISIBLE);


                        if (shortcuts.size() > 1 && shortcuts.get(1) != null) {
                            ShortcutInfo shortcut2 = shortcuts.get(1);
                            ((FactorLargeBinding) binding).shortcut2.setVisibility(View.VISIBLE);
                            Drawable icon2 = factorManager.launcherApps.getShortcutIconDrawable(shortcut2, itemView.getContext().getResources().getDisplayMetrics().densityDpi);
                            ((FactorLargeBinding) binding).shortcut2Icon.setImageDrawable(icon2);
                            ((FactorLargeBinding) binding).shortcut2Label.setText(shortcut2.getShortLabel());

                            ((FactorLargeBinding) binding).shortcut2.setOnClickListener(view -> startShortCut(shortcut2));
                        } else
                            ((FactorLargeBinding) binding).shortcut2.setVisibility(View.INVISIBLE);

                        if (shortcuts.size() > 2 && shortcuts.get(2) != null) {
                            ShortcutInfo shortcut3 = shortcuts.get(2);
                            ((FactorLargeBinding) binding).shortcut3.setVisibility(View.VISIBLE);
                            Drawable icon3 = factorManager.launcherApps.getShortcutIconDrawable(shortcut3, itemView.getContext().getResources().getDisplayMetrics().densityDpi);
                            ((FactorLargeBinding) binding).shortcut3Icon.setImageDrawable(icon3);
                            ((FactorLargeBinding) binding).shortcut3Label.setText(shortcut3.getShortLabel());

                            ((FactorLargeBinding) binding).shortcut3.setOnClickListener(view -> startShortCut(shortcut3));
                        } else
                            ((FactorLargeBinding) binding).shortcut3.setVisibility(View.INVISIBLE);
                    } else {
                        ((FactorLargeBinding) binding).shortcut1.setVisibility(View.INVISIBLE);
                        ((FactorLargeBinding) binding).shortcut2.setVisibility(View.INVISIBLE);
                        ((FactorLargeBinding) binding).shortcut3.setVisibility(View.INVISIBLE);
                        ((FactorLargeBinding) binding).shortcutAvailability.setVisibility(View.VISIBLE);
                    }


                    if (!isLiveWallpaper)
                        ((FactorLargeBinding) binding).trans
                                .setupWith(background)
                                .setOverlayColor(Color.parseColor("#" + appSettings.getTileThemeColor()))
                                .setBlurAlgorithm(new RenderScriptBlur(itemView.getContext()))
                                .setBlurRadius(appSettings.getBlurRadius())
                                .setBlurAutoUpdate(false)
                                .setHasFixedTransformationMatrix(false);
                    break;
            }

            itemView.setOnClickListener(v ->
            {
                Intent intent = factorManager.packageManager.getLaunchIntentForPackage(factor.getPackageName());
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


        //launch shortcut
        @RequiresApi(api = Build.VERSION_CODES.N_MR1)
        private void startShortCut(ShortcutInfo shortcutInfo)
        {
            factorManager.launcherApps.startShortcut(shortcutInfo.getPackage(), shortcutInfo.getId(), null, null, Process.myUserHandle());
        }
    }
}