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
    lateinit var icon: Drawable

    @Ignore
    var notificationCount : Int = 0

    object Size
    {
        const val small : Int = 1
        const val medium : Int = 2
        const val large : Int = 3
    }
}