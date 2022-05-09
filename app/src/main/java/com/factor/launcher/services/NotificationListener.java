package com.factor.launcher.services;

import android.app.Notification;
import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.factor.launcher.util.Constants;


public class NotificationListener extends NotificationListenerService
{

    //send a broadcast to NotificationBroadcastReceiver when a new notification has arrived
    @Override
    public void onNotificationPosted(StatusBarNotification sbn)
    {
        int id = sbn.getId();
        String packageName = sbn.getPackageName();
        String title = String.valueOf(sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE));
        String text = String.valueOf(sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT));


        String category = sbn.getNotification().category;


        Intent intent = new Intent(Constants.NOTIFICATION_INTENT_ACTION_POST);
        intent.putExtra(Constants.NOTIFICATION_INTENT_PACKAGE_KEY, packageName);
        intent.putExtra(Constants.NOTIFICATION_INTENT_ID_KEY, id);
        intent.putExtra(Constants.NOTIFICATION_INTENT_CONTENT_TEXT_KEY, text);
        intent.putExtra(Constants.NOTIFICATION_INTENT_TITLE_TEXT_KEY, title);
        intent.putExtra(Constants.NOTIFICATION_INTENT_CATEGORY_KEY, category);



        sendBroadcast(intent);
    }

    //send a broadcast to NotificationBroadcastReceiver when a notification has been cleared
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn)
    {
        int id = sbn.getId();
        String packageName = sbn.getPackageName();

        Intent intent = new Intent(Constants.NOTIFICATION_INTENT_ACTION_CLEAR);
        intent.putExtra(Constants.NOTIFICATION_INTENT_PACKAGE_KEY, packageName);
        intent.putExtra(Constants.NOTIFICATION_INTENT_ID_KEY, id);
        sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        getCurrentNotifications();
        return super.onStartCommand(intent, flags, startId);
    }

    //retrieve currently displayed notifications
    private void getCurrentNotifications()
    {
        try
        {
            StatusBarNotification[] currentNotifications = getActiveNotifications();
            if (currentNotifications.length > 0)
                for (StatusBarNotification notification : currentNotifications)
                    onNotificationPosted(notification);
        }
        catch (SecurityException | NullPointerException ignored){}
    }
}
