package com.factor.launcher.services;

import android.app.Notification;
import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.factor.launcher.util.Constants;

public class NotificationListener extends NotificationListenerService
{
    @Override
    public void onNotificationPosted(StatusBarNotification sbn)
    {
        int id = sbn.getId();
        String packageName = sbn.getPackageName();
        String title = String.valueOf(sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE));
        String text = String.valueOf(sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT));

        Intent intent = new  Intent(Constants.NOTIFICATION_INTENT_ACTION_POST);
        intent.putExtra(Constants.NOTIFICATION_INTENT_PACKAGE_KEY, packageName);
        intent.putExtra(Constants.NOTIFICATION_INTENT_ID_KEY, id);
        intent.putExtra(Constants.NOTIFICATION_INTENT_CONTENT_TEXT_KEY, text);
        intent.putExtra(Constants.NOTIFICATION_INTENT_TITLE_TEXT_KEY, title);
        sendBroadcast(intent);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn)
    {
        int id = sbn.getId();
        String packageName = sbn.getPackageName();

        Intent intent = new  Intent(Constants.NOTIFICATION_INTENT_ACTION_CLEAR);
        intent.putExtra(Constants.NOTIFICATION_INTENT_PACKAGE_KEY, packageName);
        intent.putExtra(Constants.NOTIFICATION_INTENT_ID_KEY, id);
        sendBroadcast(intent);
    }
}
