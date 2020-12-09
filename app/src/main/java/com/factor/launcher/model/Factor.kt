package com.factor.launcher.model

import android.graphics.drawable.Drawable
import androidx.room.*

@Entity
class Factor
{
    //todo: add auto increment primary key
    //package name is not ideal
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

    @Ignore
    lateinit var icon: Drawable
}