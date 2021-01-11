package com.factor.launcher.ui

import android.content.Context
import android.graphics.Canvas
import android.view.View
import android.widget.EdgeEffect
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.recyclerview.widget.RecyclerView.EdgeEffectFactory.DIRECTION_BOTTOM

class BouncyEdgeEffect(context: Context?, private val spring : SpringAnimation, val view : View, private val direction : Int) : EdgeEffect(context)
{

    override fun onPull(deltaDistance: Float)
    {
        super.onPull(deltaDistance)
        onPullAnimation(deltaDistance)
    }

    override fun onPull(deltaDistance: Float, displacement: Float)
    {
        super.onPull(deltaDistance, displacement)
        onPullAnimation(deltaDistance)
    }

    private fun onPullAnimation(deltaDistance: Float)
    {
        val delta =
            if (direction == DIRECTION_BOTTOM)
                -1 * view.width * deltaDistance * 0.5f
            else 1 * view.width * deltaDistance * 0.5f
        spring.cancel()
        view.translationY += delta

    }

    override fun onRelease()
    {
        super.onRelease()
        spring.start()

    }

    override fun onAbsorb(velocity: Int)
    {
        super.onAbsorb(velocity)
        val v =
            if (direction == DIRECTION_BOTTOM) -1 * velocity * 0.5f
            else 1 * velocity * 0.5f
        spring.setStartVelocity(v).start()
    }

    override fun draw(canvas: Canvas?): Boolean
    {
        setSize(0, 0)
        return super.draw(canvas)
    }
}
