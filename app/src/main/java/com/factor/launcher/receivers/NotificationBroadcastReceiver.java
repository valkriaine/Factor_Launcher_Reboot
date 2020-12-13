package com.factor.launcher.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.factor.launcher.managers.AppListManager;
import com.factor.launcher.util.Constants;

public class NotificationBroadcastReceiver extends BroadcastReceiver
{
    private final AppListManager appListManager;

    public NotificationBroadcastReceiver(AppListManager appListManager)
    {
        this.appListManager = appListManager;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        //receive new notification
        if (intent.getAction().equals(Constants.NOTIFICATION_INTENT_ACTION_POST))
        {
            appListManager.onReceiveNotification(intent);
        }
        //notification removed
        else if (intent.getAction().equals(Constants.NOTIFICATION_INTENT_ACTION_CLEAR))
        {
            appListManager.onClearNotification(intent);
        }
    }
}
