package com.factor.launcher.util

import com.factor.launcher.models.NotificationHolder


//payload class for updating notification count/message
@Suppress("unused")
class Payload (val notificationHolder: NotificationHolder, val code : String)
{
    companion object
    {
        const val NOTIFICATION_RECEIVED = "NOTIFICATION_RECEIVED"
        const val NOTIFICATION_CLEARED = "NOTIFICATION_CLEARED"
    }
}

