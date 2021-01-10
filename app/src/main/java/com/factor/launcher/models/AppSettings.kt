package com.factor.launcher.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.factor.launcher.util.Constants


@Entity
class AppSettings
{
    @PrimaryKey
    val key = Constants.PACKAGE_NAME

    @ColumnInfo(name = "corner_radius")
    var cornerRadius = 5

    @ColumnInfo(name = "tile_theme_color")
    var tileThemeColor = "FFFFFF"

    @ColumnInfo(name = "search_bar_color")
    var searchBarColor = "FFFFFF"

    @ColumnInfo(name = "is_blurred")
    var isBlurred = true

    @ColumnInfo(name = "blur_radius")
    var blurRadius = 15

    fun getTransparentTileColor() : String
    {
        return "#4D$tileThemeColor"
    }

    fun getOpaqueTileColor() : String
    {
        return "#$tileThemeColor"
    }

    fun getTransparentSearchBarColor() : String
    {
        return "#4D$searchBarColor"
    }

    fun getOpaqueSearchBarColor() : String
    {
        return "#$searchBarColor"
    }
}