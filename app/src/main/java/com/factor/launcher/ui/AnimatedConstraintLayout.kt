package com.factor.launcher.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout

@SuppressLint("ClickableViewAccessibility")
class AnimatedConstraintLayout(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs)
{
    private var isInDragAndDrop = false
    private val accelerateDecelerateInterpolator = AccelerateDecelerateInterpolator()
    fun animateToClickedState()
    {
        if (!isInDragAndDrop)
        {
            animate().scaleX(0.95f).setInterpolator(accelerateDecelerateInterpolator).duration = 50
            animate().scaleY(0.95f).setInterpolator(accelerateDecelerateInterpolator).duration = 50
        }
    }

    fun animateBackFromClickedState()
    {
        if (!isInDragAndDrop)
        {
            animate().scaleX(1f).setInterpolator(accelerateDecelerateInterpolator).duration = 50
            animate().scaleY(1f).setInterpolator(accelerateDecelerateInterpolator).duration = 50
        }
    }

    fun animateToSelectedState()
    {
        isInDragAndDrop = true
        animate().scaleX(1.1f)
            .setInterpolator(accelerateDecelerateInterpolator)
            .setDuration(200).start()
        animate().scaleY(1.1f)
            .setInterpolator(accelerateDecelerateInterpolator)
            .setDuration(200).start()
    }

    fun animateToNormalState()
    {
        isInDragAndDrop = false
        animate().scaleX(1f)
            .setInterpolator(accelerateDecelerateInterpolator)
            .setDuration(200).start()
        animate().scaleY(1f)
            .setInterpolator(accelerateDecelerateInterpolator)
            .setDuration(200).start()
    }

    init
    {
        setOnTouchListener { _: View?, event: MotionEvent ->
            when (event.action)
            {
                MotionEvent.ACTION_DOWN ->
                {
                    animateToClickedState()
                    return@setOnTouchListener false
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                {
                    animateBackFromClickedState()
                    return@setOnTouchListener false
                }
            }
            false
        }
    }
}