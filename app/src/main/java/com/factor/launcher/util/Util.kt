package com.factor.launcher.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
import java.io.ByteArrayOutputStream
import java.lang.reflect.Method

//util functions
object Util
{
    //convert dp to pixel
    fun dpToPx(value : Float, context: Context) : Float
    {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.resources.displayMetrics)
    }

    //expand notification panel
    @SuppressLint("WrongConstant", "PrivateApi")
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
    fun <T : Drawable> T.toBitmap(): Bitmap {
        if (this is BitmapDrawable) return bitmap

        val drawable: Drawable = this
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
    private fun Bitmap.toBytes(): ByteArray = ByteArrayOutputStream().use { stream ->
        compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.toByteArray()
    }
}