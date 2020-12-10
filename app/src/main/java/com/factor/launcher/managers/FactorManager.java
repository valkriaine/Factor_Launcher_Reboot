package com.factor.launcher.managers;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.factor.launcher.model.Factor;
import com.factor.launcher.model.UserApp;
import com.valkriaine.factor.BouncyRecyclerView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FactorManager
{
    private final ArrayList<Factor> userFactors = new ArrayList<>();

    private final FactorsDatabase factorsDatabase;

    public final FactorsAdapter adapter;

    private final Activity activity;

    private final Comparator<Factor> index_order= Comparator.comparingInt(Factor::getOrder);


    public FactorManager(Activity activity, ViewGroup background)
    {
        this.activity = activity;
        //this.background = background;
        adapter = new FactorsAdapter();
        factorsDatabase = Room.databaseBuilder(activity, FactorsDatabase.class, "factor_list").build();
        loadFactors();
    }


    private void loadFactors()
    {
        new Thread(()->
        {
            userFactors.addAll(factorsDatabase.factorsDao().getAll());
            PackageManager packageManager = activity.getPackageManager();
            userFactors.sort(index_order);
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
                    e.printStackTrace();
                    factorsDatabase.factorsDao().delete(f);
                }
            }
            activity.runOnUiThread(adapter::notifyDataSetChanged);
        }).start();
    }


    public void addToHome(UserApp app)
    {
        Factor factor = app.toFactor();
        if (userFactors.size() == 3)
            factor.setSize(Factor.Size.medium);
        if (userFactors.size() == 6)
            factor.setSize(Factor.Size.large);


        userFactors.add(factor);
        factor.setOrder(userFactors.indexOf(factor));
        new Thread(() ->
        {
            factorsDatabase.factorsDao().insert(factor);
            activity.runOnUiThread(()-> adapter.notifyItemInserted(factor.getOrder()));
        }).start();

    }

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

    public void updateFactor(UserApp app)
    {
        ArrayList<Factor> factorsToUpdate = getFactorsByPackage(app);
        for (Factor f : factorsToUpdate)
        {
            loadIcon(f);
            adapter.notifyItemChanged(userFactors.indexOf(f));
        }

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

    public int getFactorSize(int position)
    {
        return adapter.getItemViewType(position);
    }


    private void loadIcon(Factor f)
    {
        PackageManager packageManager = activity.getPackageManager();
        try
        {
            if (packageManager.getApplicationInfo(f.getPackageName(), 0).enabled)
                f.setIcon(packageManager.getApplicationIcon(f.getPackageName()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            new Thread(() -> factorsDatabase.factorsDao().delete(f)).start();
        }
    }





    class FactorsAdapter extends BouncyRecyclerView.Adapter
    {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            int id = 0;
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
            return new FactorsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
        {
            FactorsViewHolder factorsViewHolder = (FactorsViewHolder)holder;
            factorsViewHolder.bindFactor(userFactors.get(position));
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
        }

        @Override
        public void onItemSelected(@Nullable RecyclerView.ViewHolder viewHolder)
        {
            assert viewHolder != null;
            viewHolder.itemView.setScaleX(1.2f);
            viewHolder.itemView.setScaleY(1.2f);
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
            private final ViewDataBinding binding;

            public FactorsViewHolder(@NonNull View itemView)
            {
                super(itemView);
                binding = DataBindingUtil.bind(itemView);
            }

            public void bindFactor(Factor factor)
            {
                if (factor.getSize() == Factor.Size.small)
                {
                    ((FactorSmallBinding)binding).setFactor(factor);
                    try
                    {
                        ((FactorSmallBinding)binding).tileIcon.setImageDrawable(factor.getIcon());
                    }
                    catch (Exception e)
                    {
                        Log.d("icon", factor.getPackageName() + " " + e.getMessage());
                        loadIcon(factor);
                    }
                }
                else if (factor.getSize() == Factor.Size.medium)
                {
                    ((FactorMediumBinding)binding).setFactor(factor);
                    try
                    {
                        ((FactorMediumBinding)binding).tileIcon.setImageDrawable(factor.getIcon());
                    }
                    catch (Exception e)
                    {
                        Log.d("icon", factor.getPackageName() + " " + e.getMessage());
                        loadIcon(factor);
                    }
                }
                else if (factor.getSize() == Factor.Size.large)
                {
                    ((FactorLargeBinding)binding).setFactor(factor);
                    try
                    {
                        ((FactorLargeBinding)binding).tileIcon.setImageDrawable(factor.getIcon());
                    }
                    catch (Exception e)
                    {
                        Log.d("icon", factor.getPackageName() + " " + e.getMessage());
                        loadIcon(factor);
                    }
                }
            }
        }
    }

}
