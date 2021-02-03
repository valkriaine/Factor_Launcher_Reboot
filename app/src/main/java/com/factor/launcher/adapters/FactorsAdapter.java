package com.factor.launcher.adapters;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;
import com.factor.bouncy.BouncyRecyclerView;
import com.factor.launcher.R;
import com.factor.launcher.databinding.FactorLargeBinding;
import com.factor.launcher.databinding.FactorMediumBinding;
import com.factor.launcher.databinding.FactorSmallBinding;
import com.factor.launcher.models.AppSettings;
import com.factor.launcher.models.Factor;
import com.factor.launcher.models.NotificationHolder;
import com.factor.launcher.models.UserApp;
import com.factor.launcher.ui.AnimatedConstraintLayout;
import com.factor.launcher.util.Constants;
import com.factor.launcher.util.Payload;
import com.factor.launcher.util.Util;
import com.factor.launcher.view_models.FactorManager;
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


    public FactorsAdapter(FactorManager factorManager,
                          AppSettings appSettings,
                          Activity activity,
                          boolean isLiveWallpaper,
                          ArrayList<Factor> userFactors,
                          ViewGroup background)
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
        activity.sendBroadcast(intent);
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


        if (binding instanceof FactorSmallBinding)
        {
            ((FactorSmallBinding) binding).tile.setupTile(appSettings, isLiveWallpaper, background);
        }

        if (binding instanceof FactorMediumBinding)
        {
            ((FactorMediumBinding) binding).tile.setupTile(appSettings, isLiveWallpaper, background);
        }

        if (binding instanceof FactorLargeBinding)
        {
            ((FactorLargeBinding) binding).tile.setupTile(appSettings, isLiveWallpaper, background);
        }

        //create context menu
        view.setOnCreateContextMenuListener((menu, v, menuInfo) ->
        {
            Factor selectedFactor;
            if (binding instanceof FactorLargeBinding)
                selectedFactor = ((FactorLargeBinding) binding).getFactor();
            else if (binding instanceof FactorMediumBinding)
                selectedFactor = ((FactorMediumBinding) binding).getFactor();
            else
            {
                //noinspection ConstantConditions
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


        return new FactorsAdapter.FactorsViewHolder(view, factorManager);
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
                    tileBinding.tile.setupContent(factorToChange);
                }
                else if (userFactors.get(position).getSize() == Factor.Size.medium)
                {
                    FactorMediumBinding tileBinding = (FactorMediumBinding) binding;
                    tileBinding.tile.setupContent(factorToChange);

                }
                else if (userFactors.get(position).getSize() == Factor.Size.large)
                {
                    FactorLargeBinding tileBinding = (FactorLargeBinding) binding;
                    tileBinding.tile.setupContent(factorToChange);
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
        activity.sendBroadcast(intent);
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

        private final FactorManager factorManager;


        public FactorsViewHolder(@NonNull View itemView, FactorManager factorManager)
        {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            this.factorManager = factorManager;
        }

        public void bindFactor(Factor factor)
        {
            //determine layout based on size
            size = factor.getSize();

            switch (size)
            {
                case Factor.Size.small:

                    ((FactorSmallBinding) binding).setFactor(factor);

                    if (factor.getIcon() == null)
                        factorManager.loadIcon(factor);
                    ((FactorSmallBinding) binding).tile.setupContent(factor);

                    break;

                case Factor.Size.medium:

                    ((FactorMediumBinding) binding).setFactor(factor);

                    if (factor.getIcon() == null)
                        factorManager.loadIcon(factor);
                    ((FactorMediumBinding) binding).tile.setupContent(factor);

                    break;


                case Factor.Size.large:

                    ((FactorLargeBinding) binding).setFactor(factor);
                    if (factor.getIcon() == null)
                        factorManager.loadIcon(factor);
                    ((FactorLargeBinding) binding).tile.setupContent(factor);
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

    }
}