package com.factor.launcher.models

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

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
    var userApp : UserApp = UserApp()


    //get notification count in string form
    fun retrieveNotificationCount() : String = userApp.currentNotifications.size.toString()

    //get notification count
    fun getNotificationCount() : Int = userApp.currentNotifications.size

    fun setShortcuts(shortcuts : ArrayList<AppShortcut>)
    {
        this.userApp.shortCuts = shortcuts
    }

    fun getIcon() : Drawable? = this.userApp.icon

    fun getBitmapIcon() : Bitmap? = this.userApp.bitmapIcon

    fun setIcon(icon : Drawable)
    {
        this.userApp.icon = icon
    }

    fun setIcon(bitmap: Bitmap)
    {
        this.userApp.bitmapIcon = bitmap
    }

    object Size
    {
        const val small : Int = 1
        const val medium : Int = 2
        const val large : Int = 3
    }
}