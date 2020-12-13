package com.factor.launcher.util


//payload class for updating notification count/message
class Payload (id : Int, code : String)
{
    companion object
    {
        const val NOTIFICATION_RECEIVED = "NOTIFICATION_RECEIVED"
        const val NOTIFICATION_CLEARED = "NOTIFICATION_CLEARED"
    }

    val notificationId = id

    val notificationCode = code
}

