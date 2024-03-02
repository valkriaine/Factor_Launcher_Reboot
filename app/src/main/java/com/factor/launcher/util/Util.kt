package com.factor.launcher.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import java.io.ByteArrayOutputStream
import java.lang.reflect.Method

//util functions
object Util
{
    //convert dp to pixel
    @JvmStatic
    fun dpToPx(value : Float, context: Context) : Float
    {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.resources.displayMetrics)
    }

    //expand notification panel
    @SuppressLint("WrongConstant", "PrivateApi")
    @JvmStatic
    fun setExpandNotificationDrawer(context: Context, expand: Boolean)
    {
        try
        {
            val statusBarService = context.getSystemService("statusbar")
            val methodName =
                if (expand)
                    "expandNotificationsPanel"
                else
                    "collapsePanels"
            val statusBarManager: Class<*> = Class.forName("android.app.StatusBarManager")
            val method: Method = statusBarManager.getMethod(methodName)
            method.invoke(statusBarService)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }


    /*
    ** by ChrisDEV @ https://gist.github.com/XinyueZ
    */
    //compare drawables
    @JvmStatic
    fun <T : Drawable> T.bytesEqualTo(t: T?) = toBitmap().bytesEqualTo(t?.toBitmap(), true)

    //required for drawable comparison
    private fun Bitmap.bytesEqualTo(otherBitmap: Bitmap?, shouldRecycle: Boolean = false) = otherBitmap?.let { other ->
        if (width == other.width && height == other.height) {
            val res = toBytes().contentEquals(other.toBytes())
            if (shouldRecycle) {
                doRecycle().also { otherBitmap.doRecycle() }
            }
            res
        } else false
    } ?: kotlin.run { false }

    private fun Bitmap.doRecycle() {
        if (!isRecycled) recycle()
    }

    private fun Bitmap.toBytes(): ByteArray = ByteArrayOutputStream().use { stream ->
        compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.toByteArray()
    }

    @JvmStatic
    fun drawableToBitmap(drawable: Drawable) : Bitmap
    {
        return drawable.toBitmap()
    }

    @JvmStatic
    fun getDominantColor(resource: Bitmap): Int {
        return Palette.from(resource)
            .maximumColorCount(8)
            .generate().getDominantColor(0)
    }

    @JvmStatic
    fun getVibrantColor(resource: Bitmap): Int {
        return Palette.from(resource)
            .maximumColorCount(8)
            .generate().getVibrantColor(0)
    }

    @JvmStatic
    fun getLightVibrantColor(resource: Bitmap): Int {
        return Palette.from(resource)
            .maximumColorCount(8)
            .generate().getLightVibrantColor(0)
    }

    @JvmStatic
    fun getDarkVibrantColor(resource: Bitmap): Int {
        return Palette.from(resource)
            .maximumColorCount(8)
            .generate().getDarkVibrantColor(0)
    }

    fun getMutedColor(resource: Bitmap): Int {
        return Palette.from(resource)
            .maximumColorCount(8)
            .generate().getMutedColor(0)
    }

    @JvmStatic
    fun getLightMutedColor(resource: Bitmap): Int {
        return Palette.from(resource)
            .maximumColorCount(8)
            .generate().getLightMutedColor(0)
    }

    @JvmStatic
    fun getDarkMutedColor(resource: Bitmap): Int {
        return Palette.from(resource)
            .maximumColorCount(8)
            .generate().getDarkMutedColor(0)
    }


}