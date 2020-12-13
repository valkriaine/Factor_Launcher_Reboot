package com.factor.launcher.models

import android.graphics.drawable.Drawable
import android.view.View
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore

import androidx.room.PrimaryKey
import java.util.*

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
    var notificationCount : Int = 0

    @Ignore
    var isBeingEdited : Boolean = false


    @Ignore
    lateinit var icon: Drawable

    //... other attributes
    fun changePinnedState()
    {
        this.isPinned = !isPinned
    }

    fun toFactor(): Factor
    {
        val factor = Factor()
        factor.icon = this.icon
        factor.isCustomized = this.isCustomized
        factor.packageName = this.packageName
        factor.labelNew = this.labelNew
        factor.labelOld = this.labelOld
        factor.notificationCount = this.notificationCount
        return factor
    }

    fun retrieveNotificationCount() : String = notificationCount.toString()

    fun incrementNotificationCount() = notificationCount++

    fun decreaseNotificationCount()
    {
        when (notificationCount)
        {
            0 -> return
            else -> notificationCount --
        }
    }

    fun visibilityEditing() : Int
    {
        return if (isBeingEdited) View.VISIBLE
        else View.GONE
    }

    fun visibilityNotificationCount() : Int
    {
        return if (notificationCount > 0 && !isBeingEdited) View.VISIBLE
        else View.GONE
    }

    fun visibilityLabel() : Int
    {
        return if (!isBeingEdited) View.VISIBLE
        else View.GONE
    }

    fun getSearchReference() : String
    {
        return (packageName + labelNew + labelOld).trim('.', '_', ' ','-').toLowerCase(Locale.ROOT)
    }


    override fun equals(other: Any?): Boolean
    {
        return if (other is UserApp) this.packageName == other.packageName else false
    }

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