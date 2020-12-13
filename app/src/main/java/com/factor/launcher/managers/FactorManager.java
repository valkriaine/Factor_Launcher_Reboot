package com.factor.launcher.managers;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.*;
import androidx.annotation.NonNull;
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

    private final PackageManager packageManager;

    public final FactorsAdapter adapter;

    private final Activity activity;

    private final ViewGroup background;

    //constructor
    public FactorManager(Activity activity, ViewGroup background, PackageManager pm)
    {
        this.activity = activity;
        this.background = background;
        packageManager = pm;
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
        factor.setOrder(userFactors.indexOf(factor));
        new Thread(() ->
        {
            Log.d("add", factor.getPackageName() + " index " + factor.getOrder());
            factorsDatabase.factorsDao().insert(factor);
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
        boolean isPinned = false;
        for (Factor f : userFactors)
        {
            if (f.getPackageName().equals(app.getPackageName()))
                isPinned = true;
        }

        return isPinned;
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
        if (userFactors.contains(factor))
        {
            int position = userFactors.indexOf(factor);
            factor.setOrder(position);
            new Thread(()->
            {
                factorsDatabase.factorsDao().updateFactorInfo(factor);
                activity.runOnUiThread(() -> adapter.notifyItemChanged(position));
            }).start();
        }
        return userFactors.contains(factor);
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
                if (userFactors.contains(f))
                {
                    removeFromHome(f);
                }
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
                    if (userFactors.get(position).getSize() == Factor.Size.small)
                    {
                        FactorSmallBinding tileBinding = (FactorSmallBinding)binding;
                        Factor factorToChange = tileBinding.getFactor();

                        if (factorToChange.retrieveNotificationCountInNumber() < 1)
                            tileBinding.notificationCount.setVisibility(View.GONE);
                        else if (factorToChange.retrieveNotificationCountInNumber() > 0)
                        {
                            tileBinding.notificationCount.setVisibility(View.VISIBLE);
                            tileBinding.notificationCount.setText(factorToChange.retrieveNotificationCount());
                        }

                        //todo
                    }
                    else if (userFactors.get(position).getSize() == Factor.Size.medium)
                    {
                        FactorMediumBinding tileBinding = (FactorMediumBinding) binding;
                        Factor factorToChange = tileBinding.getFactor();

                        if (factorToChange.retrieveNotificationCountInNumber() < 1)
                        {
                            tileBinding.tileIcon.setVisibility(View.VISIBLE);
                            tileBinding.notificationCount.setVisibility(View.GONE);
                            tileBinding.notificationTitle.setVisibility(View.GONE);
                            tileBinding.notificationContent.setVisibility(View.GONE);
                            tileBinding.notificationIcon.setVisibility(View.GONE);
                        }
                        else if (factorToChange.retrieveNotificationCountInNumber() > 0)
                        {
                            tileBinding.notificationCount.setVisibility(View.VISIBLE);
                            tileBinding.notificationCount.setText(factorToChange.retrieveNotificationCount());
                            tileBinding.tileIcon.setVisibility(View.GONE);
                            tileBinding.notificationTitle.setVisibility(View.VISIBLE);
                            tileBinding.notificationContent.setVisibility(View.VISIBLE);
                            tileBinding.notificationTitle.setText(factorToChange.getUserApp().getNotificationTitle());
                            tileBinding.notificationContent.setText(factorToChange.getUserApp().getNotificationText());

                            tileBinding.notificationIcon.setImageDrawable(factorToChange.getIcon());
                            tileBinding.notificationIcon.setVisibility(View.VISIBLE);
                        }


                    }
                    else if (userFactors.get(position).getSize() == Factor.Size.large)
                    {
                        FactorLargeBinding tileBinding = (FactorLargeBinding) binding;
                        Factor factorToChange = tileBinding.getFactor();

                        if (factorToChange.retrieveNotificationCountInNumber() < 1)
                        {
                            tileBinding.tileIcon.setVisibility(View.VISIBLE);
                            tileBinding.notificationCount.setVisibility(View.GONE);
                            tileBinding.notificationTitle.setVisibility(View.GONE);
                            tileBinding.notificationContent.setVisibility(View.GONE);
                            tileBinding.notificationIcon.setVisibility(View.GONE);
                        }
                        else if (factorToChange.retrieveNotificationCountInNumber() > 0)
                        {
                            tileBinding.notificationCount.setVisibility(View.VISIBLE);
                            tileBinding.notificationCount.setText(factorToChange.retrieveNotificationCount());
                            tileBinding.tileIcon.setVisibility(View.GONE);
                            tileBinding.notificationTitle.setVisibility(View.VISIBLE);
                            tileBinding.notificationContent.setVisibility(View.VISIBLE);
                            tileBinding.notificationTitle.setText(factorToChange.getUserApp().getNotificationTitle());
                            tileBinding.notificationContent.setText(factorToChange.getUserApp().getNotificationText());

                            tileBinding.notificationIcon.setImageDrawable(factorToChange.getIcon());
                            tileBinding.notificationIcon.setVisibility(View.VISIBLE);
                        }

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
            viewHolder.itemView.setScaleX(1);
            viewHolder.itemView.setScaleY(1);
            viewHolder.itemView.setAlpha(1f);
        }

        @Override
        public void onItemSelected(@Nullable RecyclerView.ViewHolder viewHolder)
        {
            assert viewHolder != null;
            viewHolder.itemView.setScaleX(1.1f);
            viewHolder.itemView.setScaleY(1.1f);

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
                try
                {
                    size = factor.getSize();
                    //determine layout based on size
                    if (size == Factor.Size.small)
                    {
                        ((FactorSmallBinding)binding).setFactor(factor);
                        ((FactorSmallBinding) binding).tileLabel.setText(factor.getLabelNew());
                        ((FactorSmallBinding)binding).tileIcon.setImageDrawable(factor.getIcon());
                        ((FactorSmallBinding) binding).trans
                                .setupWith(background)
                                .setBlurAlgorithm(new RenderScriptBlur(activity))
                                .setBlurRadius(25F)
                                .setHasFixedTransformationMatrix(false);

                        ((FactorSmallBinding)binding).notificationCount.setVisibility(factor.visibilityNotificationCount());
                        if (factor.retrieveNotificationCountInNumber() > 0)
                            ((FactorSmallBinding)binding).notificationCount.setText(factor.retrieveNotificationCount());
                    }
                    else if (size == Factor.Size.medium)
                    {
                        ((FactorMediumBinding)binding).setFactor(factor);
                        ((FactorMediumBinding) binding).tileLabel.setText(factor.getLabelNew());
                        ((FactorMediumBinding)binding).tileIcon.setImageDrawable(factor.getIcon());
                        ((FactorMediumBinding) binding).trans
                                .setupWith(background)
                                .setBlurAlgorithm(new RenderScriptBlur(activity))
                                .setBlurRadius(18F)
                                .setHasFixedTransformationMatrix(false);

                        ((FactorMediumBinding)binding).notificationCount.setVisibility(factor.visibilityNotificationCount());
                        ((FactorMediumBinding)binding).tileIcon.setVisibility(factor.visibilityNotificationCount());
                        ((FactorMediumBinding)binding).notificationTitle.setVisibility(factor.visibilityNotificationCount());
                        ((FactorMediumBinding)binding).notificationContent.setVisibility(factor.visibilityNotificationCount());
                        ((FactorMediumBinding)binding).notificationIcon.setVisibility(factor.visibilityNotificationCount());

                        if (factor.retrieveNotificationCountInNumber() > 0)
                        {
                            ((FactorMediumBinding)binding).notificationCount.setText(factor.retrieveNotificationCount());
                            ((FactorMediumBinding)binding).tileIcon.setVisibility(View.GONE);
                            ((FactorMediumBinding)binding).notificationTitle.setText(factor.getUserApp().getNotificationTitle());
                            ((FactorMediumBinding)binding).notificationContent.setText(factor.getUserApp().getNotificationText());
                            ((FactorMediumBinding)binding).notificationIcon.setImageDrawable(factor.getIcon());
                        }
                        else
                            ((FactorMediumBinding)binding).tileIcon.setVisibility(View.VISIBLE);
                    }
                    else if (size == Factor.Size.large)
                    {
                        ((FactorLargeBinding)binding).setFactor(factor);
                        ((FactorLargeBinding)binding).tileLabel.setText(factor.getLabelNew());
                        ((FactorLargeBinding)binding).tileIcon.setImageDrawable(factor.getIcon());
                        ((FactorLargeBinding)binding).trans
                                .setupWith(background)
                                .setBlurAlgorithm(new RenderScriptBlur(activity))
                                .setBlurRadius(18F)
                                .setHasFixedTransformationMatrix(false);

                        ((FactorLargeBinding)binding).notificationCount.setVisibility(factor.visibilityNotificationCount());
                        ((FactorLargeBinding)binding).tileIcon.setVisibility(factor.visibilityNotificationCount());
                        ((FactorLargeBinding)binding).notificationTitle.setVisibility(factor.visibilityNotificationCount());
                        ((FactorLargeBinding)binding).notificationContent.setVisibility(factor.visibilityNotificationCount());
                        ((FactorLargeBinding)binding).notificationIcon.setVisibility(factor.visibilityNotificationCount());

                        if (factor.retrieveNotificationCountInNumber() > 0)
                        {
                            ((FactorLargeBinding)binding).notificationCount.setText(factor.retrieveNotificationCount());
                            ((FactorLargeBinding)binding).tileIcon.setVisibility(View.GONE);
                            ((FactorLargeBinding)binding).notificationTitle.setText(factor.getUserApp().getNotificationTitle());
                            ((FactorLargeBinding)binding).notificationContent.setText(factor.getUserApp().getNotificationText());
                            ((FactorLargeBinding)binding).notificationIcon.setImageDrawable(factor.getIcon());
                        }
                        else
                            ((FactorLargeBinding)binding).tileIcon.setVisibility(View.VISIBLE);
                    }

                    itemView.setOnCreateContextMenuListener((menu, v, menuInfo) ->
                    {
                        Factor selectedFactor = getFactor();

                        if (!selectedFactor.getPackageName().isEmpty())
                        {
                            MenuInflater inflater = activity.getMenuInflater();
                            inflater.inflate(R.menu.factor_item_menu, menu);

                            //remove from home
                            menu.getItem(0).setOnMenuItemClickListener(item -> removeFactorBroadcast(factor));

                            //resize
                            SubMenu subMenu = menu.getItem(1).getSubMenu();
                            subMenu.getItem(0).setOnMenuItemClickListener(item -> resizeFactor(factor, Factor.Size.small));
                            subMenu.getItem(1).setOnMenuItemClickListener(item -> resizeFactor(factor, Factor.Size.medium));
                            subMenu.getItem(2).setOnMenuItemClickListener(item -> resizeFactor(factor, Factor.Size.large));

                            //info
                            menu.getItem(2).setOnMenuItemClickListener(item ->
                            {
                                activity.startActivity(
                                        new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.parse("package:"+factor.getPackageName())));
                                return true;
                            });

                            //uninstall
                            menu.getItem(3).setOnMenuItemClickListener(item ->
                            {
                                activity.startActivity(new Intent(Intent.ACTION_DELETE, Uri.parse("package:"+factor.getPackageName()))
                                        .putExtra(Intent.EXTRA_RETURN_RESULT, true));
                                return true;
                            });

                            switch (factor.getSize()) {
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

                    //todo: customize activity transition animation
                    itemView.setOnClickListener(v -> {
                        Intent intent = packageManager.getLaunchIntentForPackage(factor.getPackageName());
                        if (intent != null)
                            activity.startActivity(intent,
                                    ActivityOptions.makeClipRevealAnimation(itemView,0,0,100, 100).toBundle());
                    });
                }
                catch (Exception e)
                {
                    Log.d("icon", "error loading factor for " + factor.getPackageName() + " " + e.getMessage());
                }
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
