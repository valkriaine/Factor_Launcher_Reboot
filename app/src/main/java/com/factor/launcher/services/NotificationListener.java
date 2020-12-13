package com.factor.launcher.services;

import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.factor.launcher.util.Constants;

public class NotificationListener extends NotificationListenerService
{
    @Override
    public void onNotificationPosted(StatusBarNotification sbn)
    {

        String packageName = sbn.getPackageName();
        int id = sbn.getId();

        //todo: add extras here
        Intent intent = new  Intent(Constants.NOTIFICATION_INTENT_ACTION_POST);
        intent.putExtra(Constants.NOTIFICATION_INTENT_PACKAGE_KEY, packageName);
        intent.putExtra(Constants.NOTIFICATION_INTENT_ID_KEY, id);
        sendBroadcast(intent);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn)
    {
        String packageName = sbn.getPackageName();
        int id = sbn.getId();

        //todo: add extras here
        Intent intent = new  Intent(Constants.NOTIFICATION_INTENT_ACTION_CLEAR);
        intent.putExtra(Constants.NOTIFICATION_INTENT_PACKAGE_KEY, packageName);
        intent.putExtra(Constants.NOTIFICATION_INTENT_ID_KEY, id);
        sendBroadcast(intent);
    }
}
