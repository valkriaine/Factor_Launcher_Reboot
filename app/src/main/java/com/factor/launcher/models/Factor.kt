package com.factor.launcher.models

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Drawable
import android.view.View
import androidx.room.*

@Entity
class Factor (@PrimaryKey val packageName: String)
{
    @ColumnInfo(name = "order")
    var order: Int = 0

    @ColumnInfo(name = "labelNew")
    var labelNew: String = ""
        set(value)
        {
            userApp.labelNew = value
            field = userApp.labelNew
        }

    @ColumnInfo(name = "size")
    var size: Int = Size.small

    @ColumnInfo(name = "is_widget")
    var isWidget : Boolean = false

    @ColumnInfo(name = "widget_id")
    var widgetId : Int = -1

    @Ignore
    var userApp : UserApp = UserApp()
    set(value)
    {
        field = value
        labelNew = value.labelNew
    }

    @Ignore
    var widgetHostView : AppWidgetHostView? = null


    fun getWidgetHostView(appWidgetHost : AppWidgetHost, appWidgetManager : AppWidgetManager, context : Context) : AppWidgetHostView
    {
        if (this.widgetHostView != null) return this.widgetHostView as AppWidgetHostView

        this.widgetHostView = appWidgetHost.createView(context, widgetId, appWidgetManager.getAppWidgetInfo(widgetId))
        this.widgetHostView?.setOnLongClickListener { false }
        this.widgetHostView?.isClickable = true
        return widgetHostView as AppWidgetHostView
    }

    fun createWidgetHostView(appWidgetHost : AppWidgetHost, appWidgetManager : AppWidgetManager, context : Context)
    {
        this.widgetHostView = null
        this.widgetHostView = appWidgetHost.createView(context, widgetId, appWidgetManager.getAppWidgetInfo(widgetId))
        this.widgetHostView?.setOnLongClickListener { false }
        this.widgetHostView?.isClickable = true
    }


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

    fun setShortcuts(shortcuts : List<ShortcutInfo>)
    {
        this.userApp.shortCuts = shortcuts
    }

    fun getIcon() : Drawable = this.userApp.icon

    fun setIcon(icon : Drawable)
    {
        this.userApp.icon = icon
    }

    object Size
    {
        const val small : Int = 1
        const val medium : Int = 2
        const val large : Int = 3
        const val widget : Int = 4
    }
}