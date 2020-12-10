package com.factor.launcher.models

import android.graphics.drawable.Drawable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore

import androidx.room.PrimaryKey

@Entity
class UserApp
{
    @PrimaryKey
    var packageName: String = ""

    @ColumnInfo(name = "labelOld")
    var labelOld: String = ""

    @ColumnInfo(name = "labelNew")
    var labelNew: String = ""

    @Ignore
    lateinit var icon: Drawable

    @ColumnInfo(name = "pinned")
    var isPinned: Boolean = false

    @ColumnInfo(name = "is_customized")
    var isCustomized: Boolean = false

    @ColumnInfo(name = "hidden")
    var isHidden: Boolean = false

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

        return factor
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