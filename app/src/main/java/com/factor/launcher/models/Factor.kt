package com.factor.launcher.models

import android.graphics.drawable.Drawable
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
    var userApp : UserApp = UserApp()

    //get notification count
    fun retrieveNotificationCount() : String = userApp.currentNotifications.size.toString()


    fun setShortcuts(shortcuts : ArrayList<AppShortcut>)
    {
        this.userApp.shortCuts = shortcuts
    }

    fun getIcon() : Drawable? = this.userApp.icon

    fun setIcon(icon : Drawable)
    {
        this.userApp.icon = icon
    }

    object Size
    {
        const val small : Int = 1
        const val medium : Int = 2
        const val large : Int = 3
    }
}