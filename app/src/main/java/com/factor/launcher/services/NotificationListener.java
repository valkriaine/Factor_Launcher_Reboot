package com.factor.launcher.services;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.factor.launcher.util.Constants;


public class NotificationListener extends NotificationListenerService
{
    private ComponentsSetupReceiver componentsSetupReceiver;

    //register components setup receiver when notification listener is created
    @Override
    public void onCreate()
    {
        super.onCreate();
        componentsSetupReceiver = new ComponentsSetupReceiver(this);
        registerReceiver(componentsSetupReceiver, new IntentFilter(Constants.NOTIFICATION_INTENT_ACTION_SETUP));
    }

    //unregister components setup receiver on destroy
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (componentsSetupReceiver != null)
        {
            unregisterReceiver(componentsSetupReceiver);
            componentsSetupReceiver.invalidate();
        }
    }

    //send a broadcast to NotificationBroadcastReceiver when a new notification has arrived
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

    //send a broadcast to NotificationBroadcastReceiver when a notification has been cleared
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


    //retrieve currently displayed notifications
    private void getCurrentNotifications()
    {
        StatusBarNotification[] currentNotifications = getActiveNotifications();
        if (currentNotifications.length > 0)
            for (StatusBarNotification notification : currentNotifications)
                onNotificationPosted(notification);
    }


    //receive broadcast when app has successfully registered all other receivers
    private static class ComponentsSetupReceiver extends BroadcastReceiver
    {

        private NotificationListener listener;

        public ComponentsSetupReceiver(NotificationListener listener)
        {
            this.listener = listener;
        }

        public void invalidate()
        {
            listener = null;
        }

        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent.getAction().equals(Constants.NOTIFICATION_INTENT_ACTION_SETUP))
            {
                try
                {
                    listener.getCurrentNotifications();
                }
                catch (Exception ignored){}
            }
        }
    }

}
