package com.factor.launcher.managers;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import com.factor.launcher.R;
import com.factor.launcher.database.FactorsDatabase;
import com.factor.launcher.databinding.FactorBinding;
import com.factor.launcher.model.Factor;
import com.factor.launcher.model.UserApp;
import com.factor.launcher.util.Constants;
import com.valkriaine.factor.BouncyRecyclerView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FactorManager
{
    private final ArrayList<Factor> userFactors = new ArrayList<>();

    private final FactorsDatabase factorsDatabase;

    public final FactorsAdapter adapter;

    private final Activity activity;


    private final Comparator<Factor> index_order= Comparator.comparingInt(Factor::getOrder);


    public FactorManager(Activity activity)
    {
        this.activity = activity;
        adapter = new FactorsAdapter();
        factorsDatabase = Room.databaseBuilder(activity, FactorsDatabase.class, "factor_list").build();
        loadFactors();
    }


    private void loadFactors()
    {
        new Thread(()->
        {
            PackageManager packageManager = activity.getPackageManager();
            userFactors.addAll(factorsDatabase.factorsDao().getAll());
            for (Factor f: userFactors)
            {
                try
                {
                    if (doesPackageExist(f) && packageManager.getApplicationInfo(f.getPackageName(), 0).enabled)
                        f.setIcon(packageManager.getApplicationIcon(f.getPackageName()));
                    else
                        factorsDatabase.factorsDao().delete(f);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            userFactors.sort(index_order);
            activity.runOnUiThread(adapter::notifyDataSetChanged);
        }).start();
    }

    public void addToHome(UserApp app)
    {
        Factor factor = app.toFactor();
        userFactors.add(factor);
        factor.setOrder(userFactors.indexOf(factor));
        factorsDatabase.factorsDao().insert(factor);
        activity.runOnUiThread(()-> adapter.notifyItemInserted(factor.getOrder()));
    }

    public void removeFromHome(Factor factor)
    {
        userFactors.remove(factor);

        factorsDatabase.factorsDao().delete(factor);
        updateOrders();
        activity.runOnUiThread(adapter::notifyDataSetChanged);

    }

    public void updateFactor()
    {

    }

    private void updateOrders()
    {
        for (Factor f: userFactors)
        {
            f.setOrder(userFactors.indexOf(f));
            new Thread(()->factorsDatabase.factorsDao().updateFactorInfo(f)).start();
        }
    }


    public void remove(UserApp app)
    {
        ArrayList<Factor> factorsToRemove = getFactorsByPackage(app);
        for (Factor f : factorsToRemove)
        {
            if (userFactors.contains(f))
            {
                removeFromHome(f);
            }
        }
    }

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


    private boolean doesPackageExist(Factor f)
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
                if (r.activityInfo.packageName.equals(f.getPackageName()))
                {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }




    class FactorsAdapter extends BouncyRecyclerView.Adapter
    {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.factor, parent, false);
            return new FactorsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
        {
            FactorsViewHolder factorsViewHolder = (FactorsViewHolder)holder;
            factorsViewHolder.bindFactor(userFactors.get(position));

            //this prevents item views from collapsing
            factorsViewHolder.setIsRecyclable(false);
        }


        @Override
        public int getItemCount()
        {
            return userFactors.size();
        }

        @Override
        public void onItemMoved(int fromPosition, int toPosition)
        {

            if (fromPosition < toPosition)
            {
                for (int i = fromPosition; i < toPosition; i++)
                {
                    Collections.swap(userFactors, i, i + 1);
                }
            }
            else
                {
                for (int i = fromPosition; i > toPosition; i--)
                {
                    Collections.swap(userFactors, i, i - 1);
                }
            }

            notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onItemReleased(@Nullable RecyclerView.ViewHolder viewHolder)
        {
           updateOrders();
        }

        @Override
        public void onItemSelected(@Nullable RecyclerView.ViewHolder viewHolder)
        {

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
            private final FactorBinding binding;

            public FactorsViewHolder(@NonNull View itemView)
            {
                super(itemView);
                binding = DataBindingUtil.bind(itemView);
            }

            public void bindFactor(Factor factor)
            {
                binding.setFactor(factor);
                try
                {
                    binding.tileIcon.setImageDrawable(factor.getIcon());
                }
                catch (Exception e)
                {
                    //todo: for some reason disabled apps are not removed from database
                    Log.d("", factor.getPackageName());
                    new Thread(() ->removeFromHome(factor)).start();

                }

            }
        }
    }

}
