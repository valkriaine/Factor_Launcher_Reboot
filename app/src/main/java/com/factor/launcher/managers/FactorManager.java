package com.factor.launcher.managers;

import android.app.Activity;
import android.content.pm.PackageManager;
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
import com.valkriaine.factor.BouncyRecyclerView;
import org.jetbrains.annotations.Nullable;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FactorManager
{
    private final ArrayList<Factor> userFactors = new ArrayList<>();

    private final FactorsDatabase factorsDatabase;

    public final FactorsAdapter adapter;

    private final Activity activity;


    private final Comparator<Factor> index_order= new Comparator<Factor>()
    {
        private final Collator sCollator = Collator.getInstance();
        @Override
        public int compare(Factor f1, Factor f2)
        {
            return f1.getOrder() - f2.getOrder();
        }
    };


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
            userFactors.sort(index_order);
            for (Factor f: userFactors)
            {
                try
                {
                    f.setIcon(packageManager.getApplicationIcon(f.getPackageName()));
                }
                catch (PackageManager.NameNotFoundException e)
                {
                    e.printStackTrace();
                }
            }
            activity.runOnUiThread(adapter::notifyDataSetChanged);
        }).start();
    }

    public void addToHome(UserApp app)
    {
        Factor factor = app.toFactor();
        userFactors.add(factor);
        factor.setOrder(userFactors.indexOf(factor));

        //save to db
        new Thread(()->
        {
            factorsDatabase.factorsDao().insert(factor);
            activity.runOnUiThread(()-> adapter.notifyItemInserted(factor.getOrder()));

        }).start();
    }

    public void removeFromHome(Factor factor)
    {
        int position = userFactors.indexOf(factor);
        userFactors.remove(factor);
        new Thread(()->
        {
            factorsDatabase.factorsDao().delete(factor);
            activity.runOnUiThread(()->adapter.notifyItemRemoved(position));
        });
    }

    public void removeFromHome(UserApp userApp)
    {
        Factor factorToRemove = null;
        for (Factor f:userFactors)
        {
            if (f.getPackageName().equals(userApp.getPackageName()))
                factorToRemove = f;
        }

        if (factorToRemove != null)
        {
            int position = userFactors.indexOf(factorToRemove);
            userFactors.remove(factorToRemove);
            Factor finalFactorToRemove = factorToRemove;
            new Thread(()->
            {
                factorsDatabase.factorsDao().delete(finalFactorToRemove);
                activity.runOnUiThread(()->adapter.notifyItemRemoved(position));
            });
        }

    }

    public void updateFactor()
    {

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
            for (Factor f: userFactors)
            {
                f.setOrder(userFactors.indexOf(f));
                new Thread(()->factorsDatabase.factorsDao().updateFactorInfo(f)).start();
            }
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
            }
        }
    }

}
