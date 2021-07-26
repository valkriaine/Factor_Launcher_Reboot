package com.factor.launcher.view_models;

import android.content.pm.PackageManager;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.factor.launcher.adapters.RecentAppsAdapter;
import com.factor.launcher.models.UserApp;

import java.util.LinkedList;

public class RecentAppsHost extends ViewModel
{
    private final MutableLiveData<LinkedList<UserApp>> recentAppsMutableLiveData = new MutableLiveData<>();

    private final LinkedList<UserApp> recentApps = new LinkedList<>();

    private static final int MAX_SIZE = 3;

    private final RecentAppsAdapter adapter;

    public RecentAppsHost(PackageManager packageManager)
    {
        adapter = new RecentAppsAdapter(recentApps, packageManager);
        recentAppsMutableLiveData.setValue(recentApps);
    }

    public RecentAppsAdapter getAdapter()
    {
        return this.adapter;
    }

    private boolean contains(UserApp a)
    {
        for (UserApp app : recentApps)
        {
            if (app.getPackageName().equals(a.getPackageName()))
                return true;
        }
        return false;
    }

    public void add(UserApp app)
    {
        if (app.getPackageName().isEmpty())
            return;


        //if app already in recent apps, move it to the last
        if (contains(app))
        {
            recentApps.remove(app);
        }
        else //app not in list
        {
            if (recentApps.size() >= MAX_SIZE)
                recentApps.removeLast();
        }

        recentApps.addFirst(app);
        adapter.notifyDataSetChanged();
    }

    public void remove(UserApp app)
    {
        if (recentApps.contains(app))
        {
            int position = recentApps.indexOf(app);
            recentApps.remove(app);
            adapter.notifyItemRemoved(position);
        }

    }

    public void update(UserApp app)
    {
        if (recentApps.contains(app))
        {
            int position = recentApps.indexOf(app);
            recentApps.set(position, app);
            adapter.notifyItemChanged(position);
        }
    }


    public MutableLiveData<LinkedList<UserApp>> getRecentAppsMutableLiveData()
    {
        return recentAppsMutableLiveData;
    }
}
