package com.factor.launcher.model

import android.graphics.drawable.Drawable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore

import androidx.room.PrimaryKey

@Entity
class UserApp
{
    @PrimaryKey
    var name: String = ""

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

    override fun equals(other: Any?): Boolean
    {
        return if (other is UserApp) this.name.equals(other.name) else false
    }
}