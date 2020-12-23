package com.factor.launcher.models

import android.content.pm.ShortcutInfo
import android.graphics.drawable.Drawable
import android.view.View
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore

import androidx.room.PrimaryKey
import java.util.*
import kotlin.collections.ArrayList

@Entity
class UserApp
{
    @PrimaryKey
    var packageName: String = ""

    @ColumnInfo(name = "labelOld")
    var labelOld: String = ""

    @ColumnInfo(name = "labelNew")
    var labelNew: String = ""

    @ColumnInfo(name = "pinned")
    var isPinned: Boolean = false

    @ColumnInfo(name = "is_customized")
    var isCustomized: Boolean = false

    @ColumnInfo(name = "hidden")
    var isHidden: Boolean = false

    @ColumnInfo(name = "color")
    var color : String = ""

    @Ignore
    var isBeingEdited : Boolean = false

    @Ignore
    var shortCuts : List<ShortcutInfo> = ArrayList()

    @Ignore
    val currentNotifications : ArrayList<NotificationHolder> = ArrayList()

    @Ignore
    lateinit var icon: Drawable


    //... other attributes
    fun changePinnedState()
    {
        this.isPinned = !isPinned
    }

    //generate new factor
    fun toFactor(): Factor
    {
        val factor = Factor(this.packageName)
        factor.userApp = this
        return factor
    }

    //get notification count
    fun retrieveNotificationCount() : String = currentNotifications.size.toString()

    //increment notification count when new notifications arrive, only increment if notification id is different
    fun incrementNotificationCount(notificationHolder: NotificationHolder) : Boolean
    {
        currentNotifications.forEach{
            notification -> if (notification.id == notificationHolder.id)
            {
                return if (notification.title != notificationHolder.title || notification.text != notificationHolder.text)
                {
                    currentNotifications[currentNotifications.indexOf(notification)] = notificationHolder
                    true
                }
                else false
            }
        }
        currentNotifications.add(notificationHolder)
        return true
    }

    fun getNotificationTitle() : String
    {
        return if (currentNotifications.size > 0)
        {
            val text = currentNotifications.last().title
            if (text == "null" || text == "Null" || text.isEmpty())
                labelNew
            else
                text
        }
        else
            "You are all caught up"
    }

    fun getNotificationText() : String
    {
        return if (currentNotifications.size > 0)
        {
            val text = currentNotifications.last().text
            if (text == "null" || text == "Null" || text.isEmpty())
                "Notification from $labelNew"
            else
                text
        } else
            "No notification"
    }


    //decrease notification count when a notification is dismissed, only decrease if notification has record here
    fun decreaseNotificationCount(id : Int) : Boolean
    {
        currentNotifications.forEach{
            if (it.id == id)
            {
                currentNotifications.remove(it)
                return true
            }
        }
        return false
    }

    //for data binding, only return VISIBLE if the app is being edited
    fun visibilityEditing() : Int
    {
        return if (isBeingEdited) View.VISIBLE
        else View.GONE
    }

    //for data binding, only return VISIBLE if notification count is greater than 0 and the app is not being edited
    fun visibilityNotificationCount() : Int
    {
        return if (currentNotifications.size > 0 && !isBeingEdited) View.VISIBLE
        else View.GONE
    }

    //for data binding, only return VISIBLE if the app is not being edited
    fun visibilityLabel() : Int
    {
        return if (!isBeingEdited) View.VISIBLE
        else View.GONE
    }

    //return search reference consisting of package name, labelNew, and labelOld
    fun getSearchReference() : String
    {
        return (packageName + labelNew + labelOld).trim('.', '_', ' ','-').toLowerCase(Locale.ROOT)
    }

    fun hasShortcuts() : Boolean = shortCuts.isNotEmpty()

    //equal if package names are the same
    override fun equals(other: Any?): Boolean
    {
        return if (other is UserApp) this.packageName == other.packageName else false
    }

    //hash code
    override fun hashCode(): Int {
        var result = packageName.hashCode()
        result = 31 * result + labelOld.hashCode()
        result = 31 * result + labelNew.hashCode()
        result = 31 * result + icon.hashCode()
        result = 31 * result + isPinned.hashCode()
        result = 31 * result + isCustomized.hashCode()
        result = 31 * result + isHidden.hashCode()
        return result
    }
}