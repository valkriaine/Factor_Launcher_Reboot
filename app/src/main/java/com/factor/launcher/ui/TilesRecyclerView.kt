package com.factor.launcher.ui

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.EdgeEffect
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.factor.bouncy.util.DragDropAdapter
import com.factor.bouncy.util.DragDropCallBack
import com.factor.bouncy.util.OnOverPullListener

class TilesRecyclerView(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs)
{
    private lateinit var callBack: DragDropCallBack

    var onOverPullListener: OnOverPullListener? = null

    var overscrollAnimationSize = 0.5f

    var flingAnimationSize = 0.5f

    @Suppress("MemberVisibilityCanBePrivate")
    var longPressDragEnabled = false
        set(value)
        {
            field = value
            if (adapter is DragDropAdapter) callBack.setDragEnabled(value)
        }

    @Suppress("MemberVisibilityCanBePrivate")
    var itemSwipeEnabled = false
        set(value)
        {
            field = value
            if (adapter is DragDropAdapter) callBack.setSwipeEnabled(value)
        }

    val spring: SpringAnimation = SpringAnimation(this, SpringAnimation.TRANSLATION_Y)
        .setSpring(
            SpringForce()
                .setFinalPosition(0f)
                .setDampingRatio(SpringForce.DAMPING_RATIO_NO_BOUNCY)
                .setStiffness(SpringForce.STIFFNESS_LOW)
        )

    override fun setAdapter(adapter: Adapter<*>?)
    {
        super.setAdapter(adapter)

        if (adapter is DragDropAdapter)
        {
            callBack = DragDropCallBack(adapter, longPressDragEnabled, itemSwipeEnabled)
            val touchHelper = ItemTouchHelper(callBack)
            touchHelper.attachToRecyclerView(this)
        }
    }


    init {

        val rc = this

        //create edge effect
        this.edgeEffectFactory = object : RecyclerView.EdgeEffectFactory()
        {
            override fun createEdgeEffect(recyclerView: RecyclerView, direction: Int): EdgeEffect
            {
                return object : EdgeEffect(recyclerView.context)
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
                        if (direction == DIRECTION_BOTTOM)
                            onOverPullListener?.onOverPulledBottom(deltaDistance)
                        else if (direction == DIRECTION_TOP)
                            onOverPullListener?.onOverPulledTop(deltaDistance)
                    }

                    private fun onPullAnimation(deltaDistance: Float)
                    {
                        val delta =
                            if (direction == DIRECTION_BOTTOM)
                                -1 * recyclerView.width * deltaDistance * overscrollAnimationSize
                            else 1 * recyclerView.width * deltaDistance * overscrollAnimationSize
                        spring.cancel()
                        rc.translationY += delta
                    }

                    override fun onRelease()
                    {
                        super.onRelease()
                        onOverPullListener?.onRelease()
                        spring.start()
                    }

                    override fun onAbsorb(velocity: Int)
                    {
                        super.onAbsorb(velocity)
                        val v =
                            if (direction == DIRECTION_BOTTOM) -1 * velocity * flingAnimationSize
                            else 1 * velocity * flingAnimationSize
                        spring.setStartVelocity(v).start()
                    }

                    override fun draw(canvas: Canvas?): Boolean
                    {
                        setSize(0, 0)
                        return super.draw(canvas)
                    }
                }
            }
        }
    }
}