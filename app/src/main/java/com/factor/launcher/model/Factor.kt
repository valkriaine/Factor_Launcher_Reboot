package com.factor.launcher.model

import android.graphics.drawable.Drawable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
class Factor
{
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null

    @ColumnInfo(name = "packageName")
    var packageName: String = ""

    @ColumnInfo(name = "labelOld")
    var labelOld: String = ""

    @ColumnInfo(name = "labelNew")
    var labelNew: String = ""

    @ColumnInfo(name = "is_customized")
    var isCustomized: Boolean = false

    @ColumnInfo(name = "order")
    var order: Int = 0

    @Ignore
    lateinit var icon: Drawable
}