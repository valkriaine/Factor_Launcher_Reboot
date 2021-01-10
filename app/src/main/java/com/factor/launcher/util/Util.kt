package com.factor.launcher.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
import java.io.ByteArrayOutputStream

//util functions
object Util
{
    //convert dp to pixel
    fun dpToPx(value : Float, context: Context) : Float
    {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.resources.displayMetrics)
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
    private fun <T : Drawable> T.toBitmap(): Bitmap {
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