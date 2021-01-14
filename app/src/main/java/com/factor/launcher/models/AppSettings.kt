package com.factor.launcher.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.factor.launcher.util.Constants


@Entity
class AppSettings
{
    @PrimaryKey
    var key = Constants.PACKAGE_NAME

    @ColumnInfo(name = "corner_radius")
    var cornerRadius = 5

    @ColumnInfo(name = "tile_theme_color")
    var tileThemeColor = "99FFFFFF"

    @ColumnInfo(name = "search_bar_color")
    var searchBarColor = "4DFFFFFF"

    @ColumnInfo(name = "is_blurred")
    var isBlurred = true

    @ColumnInfo(name = "blur_radius")
    var blurRadius = 15

    @ColumnInfo(name = "isDarkText")
    var isDarkText = true

    @ColumnInfo(name = "isDarkIcon")
    var isDarkIcon = false

    @ColumnInfo(name = "icon_shadow")
    var showShadowAroundIcon = true
}