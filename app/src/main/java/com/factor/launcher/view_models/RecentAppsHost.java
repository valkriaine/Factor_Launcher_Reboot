package com.factor.launcher.view_models;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.lifecycle.ViewModel;
import com.factor.launcher.adapters.RecentAppsAdapter;
import com.factor.launcher.models.UserApp;

import java.util.LinkedList;

public class RecentAppsHost extends ViewModel
{
    private static final String KEY = "RECENT_APPS_HOST_KEY";

    private static final String STRING_KEY = "RECENT_APPS_HOST_KEY";

    private static final int MAX_SIZE = 6;

    private final LinkedList<String> recentApps = new LinkedList<>();

    private final RecentAppsAdapter adapter;

    private final SharedPreferences preferences;


    public RecentAppsHost(AppListManager appListManager)
    {
        preferences = appListManager.getActivity().getSharedPreferences(KEY, Context.MODE_PRIVATE);

        String retrieved = preferences.getString(STRING_KEY, "");

        String[] list = retrieved.split(",");

        if (list.length > 0)
        {
            for (String name : list)
            {
                if (recentApps.size() < MAX_SIZE)
                    recentApps.addLast(name);
            }
        }

        adapter = new RecentAppsAdapter(recentApps, appListManager);
    }

    public RecentAppsAdapter getAdapter()
    {
        return this.adapter;
    }

    public void save()
    {
        StringBuilder sb = new StringBuilder();
        for (String name : recentApps)
        {
           sb.append(name).append(',');
        }
        preferences.edit().putString(STRING_KEY, sb.toString()).apply();
    }

    private boolean contains(UserApp a)
    {
        for (String name : recentApps)
        {
            if (name.equals(a.getPackageName()))
                return true;
        }
        return false;
    }

    public void add(UserApp app)
    {
        if (app.getPackageName().isEmpty())
            return;


        //if app already in recent apps, move it to the last
        int position;
        if (contains(app))
        {
            position = recentApps.indexOf(app.getPackageName());
            recentApps.remove(app.getPackageName());
            adapter.notifyItemRemoved(position);
        }
        else //app not in list
        {
            if (recentApps.size() >= MAX_SIZE)
            {
                position = recentApps.size() - 1;
                recentApps.removeLast();
                adapter.notifyItemRemoved(position);
            }

        }

        recentApps.addFirst(app.getPackageName());
        adapter.notifyItemInserted(0);
        adapter.addRecentBroadcast();
    }

    public void remove(UserApp app)
    {
        if (recentApps.contains(app.getPackageName()))
        {
            int position = recentApps.indexOf(app.getPackageName());
            recentApps.remove(app.getPackageName());
            adapter.notifyItemRemoved(position);
        }

    }

    public void update(UserApp app)
    {
        if (recentApps.contains(app.getPackageName()))
            adapter.notifyItemChanged(recentApps.indexOf(app.getPackageName()));
    }
}
