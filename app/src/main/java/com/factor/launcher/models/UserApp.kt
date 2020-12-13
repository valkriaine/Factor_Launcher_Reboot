package com.factor.launcher.models

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

    @Ignore
    var isBeingEdited : Boolean = false

    @Ignore
    val currentNotifications : ArrayList<Int> = ArrayList()

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
        val factor = Factor()
        factor.icon = this.icon
        factor.isCustomized = this.isCustomized
        factor.packageName = this.packageName
        factor.labelNew = this.labelNew
        factor.labelOld = this.labelOld
        factor.currentNotifications = this.currentNotifications
        return factor
    }

    //get notification count
    fun retrieveNotificationCount() : String = currentNotifications.size.toString()

    //increment notification count when new notifications arrive, only increment if notification id is different
    fun incrementNotificationCount(id : Int) : Boolean
    {
        return if (!currentNotifications.contains(id))
        {
            currentNotifications.add(id)
            true
        }
        else false
    }

    //decrease notification count when a notification is dismissed, only decrease if notification has record here
    fun decreaseNotificationCount(id : Int) : Boolean
    {
        return if (currentNotifications.contains(id))
        {
            currentNotifications.remove(id)
            true
        }
        else false
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