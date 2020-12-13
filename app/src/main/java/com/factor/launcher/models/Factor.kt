package com.factor.launcher.models

import android.graphics.drawable.Drawable
import android.view.View
import androidx.room.*

@Entity
class Factor
{
    @PrimaryKey
    var packageName: String = ""

    @ColumnInfo(name = "labelOld")
    var labelOld: String = ""

    @ColumnInfo(name = "labelNew")
    var labelNew: String = ""

    @ColumnInfo(name = "is_customized")
    var isCustomized: Boolean = false

    @ColumnInfo(name = "order")
    var order: Int = 0

    @ColumnInfo(name = "size")
    var size: Int = Size.small

    @Ignore
    lateinit var icon: Drawable

    @Ignore
    var userApp : UserApp = UserApp()

    //get notification count
    fun retrieveNotificationCount() : String = userApp.currentNotifications.size.toString()

    //get notification count
    fun retrieveNotificationCountInNumber() : Int = userApp.currentNotifications.size

    //for data binding, only return VISIBLE if notification count is greater than 0 and the app is not being edited
    fun visibilityNotificationCount() : Int
    {
        return if (userApp.currentNotifications.size > 0) View.VISIBLE
        else View.GONE
    }

    object Size
    {
        const val small : Int = 1
        const val medium : Int = 2
        const val large : Int = 3
    }
}