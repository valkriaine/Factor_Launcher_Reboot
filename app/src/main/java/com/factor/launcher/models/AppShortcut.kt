package com.factor.launcher.models

import android.graphics.drawable.Drawable
import android.view.View

//holder object for app shortcuts
class AppShortcut (private val isStatic : Boolean,
                   var rank : Int,
                   val label : CharSequence?,
                   val icon : Drawable?,
                   val launchEvent : View.OnClickListener?) : Comparable<AppShortcut>
{
    override fun compareTo(other: AppShortcut): Int
    {
        return if (this.isStatic && other.isStatic)
            if (this.rank > other.rank) 1 else -1
        else if (!this.isStatic && !other.isStatic)
            if (this.rank < other.rank) 1 else -1
        else
        {
            if (this.isStatic) -1 else 1
        }
    }
}