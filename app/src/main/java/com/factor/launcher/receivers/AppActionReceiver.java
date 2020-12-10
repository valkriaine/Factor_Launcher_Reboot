package com.factor.launcher.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import com.factor.launcher.managers.AppListManager;
import com.factor.launcher.util.Constants;


//when a factor is removed from home, change its pinned state in the app drawer
public class AppActionReceiver extends BroadcastReceiver
{
    private final AppListManager appListManager;

    public AppActionReceiver(AppListManager a)
    {
        appListManager = a;
        IntentFilter filter = new IntentFilter(Constants.BROADCAST_ACTION_REMOVE);
        a.getActivity().registerReceiver(this, filter);
    }


    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();

        String packageName;

        if (action.equals(Constants.BROADCAST_ACTION_REMOVE))
        {
            packageName = intent.getStringExtra(Constants.REMOVE_KEY);
            Log.d("receiver", "removing " + packageName);
            appListManager.unPin(packageName);
        }
    }
}
