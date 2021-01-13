package com.factor.launcher.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager


class HomePager(context: Context?, attrs: AttributeSet?) : ViewPager(context!!, attrs)
{
    private var isSwipeAllowed = true
    private var initialXValue = 0f
    private var diff = 0f

    private var views: ArrayList<View> = ArrayList()
    private val adapter: HomePagerAdapter = HomePagerAdapter(views)

    override fun onAttachedToWindow()
    {
        super.onAttachedToWindow()
        this.offscreenPageLimit = views.count() - 1
        this.setAdapter(adapter)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean
    {
        return if (isSwipeAllowed(event))
        {
            super.onTouchEvent(event)
        }
        else false
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean
    {
        return if (isSwipeAllowed(event))
        {
            super.onInterceptTouchEvent(event)
        } else false
    }

    private fun isSwipeAllowed(event: MotionEvent): Boolean
    {

        if (!isSwipeAllowed)
            return false

        if (event.action == MotionEvent.ACTION_DOWN)
        {
            initialXValue = event.x
            return true
        }

        if (event.action == MotionEvent.ACTION_MOVE)
        {
            diff = event.x - initialXValue
            if (diff > 0 && this.currentItem == 0)
                return false
            if (diff < 0 && this.currentItem == adapter.count - 1)
                return false
        }
        return true

    }

    override fun addView(view: View, position: Int)
    {
        if (!views.contains(view))
        {
            views.add(position, view)
            adapter.notifyDataSetChanged()
        }
    }

    override fun addView(view: View)
    {
        if (!views.contains(view))
        {
            views.add(view)
            adapter.notifyDataSetChanged()
        }
    }

    internal class HomePagerAdapter(private val views: ArrayList<View>) : PagerAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            return views[position]
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun getCount(): Int {
            return views.count()
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        }
    }
}