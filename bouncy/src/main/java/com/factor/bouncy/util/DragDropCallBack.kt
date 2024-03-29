package com.factor.bouncy.util

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView


class DragDropCallBack(private val adapter: RecyclerView.Adapter<*>,
                       private var longPressDragEnabled: Boolean,
                       private var itemSwipeEnabled: Boolean) : ItemTouchHelper.Callback()
{


    override fun isLongPressDragEnabled(): Boolean
    {
        return longPressDragEnabled
    }

    override fun isItemViewSwipeEnabled(): Boolean
    {
        return itemSwipeEnabled
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int)
    {
        if (isItemViewSwipeEnabled && adapter is DragDropAdapter<*>)
        {
            if (i == ItemTouchHelper.START)
                adapter.onItemSwipedToStart(viewHolder, viewHolder.absoluteAdapterPosition)
            else if (i == ItemTouchHelper.END)
                adapter.onItemSwipedToEnd(viewHolder, viewHolder.absoluteAdapterPosition)
        }
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int
    {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean
    {
        return if (isLongPressDragEnabled && adapter is DragDropAdapter<*>)
        {
            adapter.onItemMoved(viewHolder.absoluteAdapterPosition, target.absoluteAdapterPosition)
            true
        }
        else
            false
    }

    //todo: customize swipe animation
    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    )
    {

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int)
    {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE && adapter is DragDropAdapter<*>)
        {
            adapter.onItemSelected(viewHolder)
        }
        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder)
    {
        super.clearView(recyclerView, viewHolder)
        if (adapter is DragDropAdapter<*>)
            adapter.onItemReleased(viewHolder)
    }

    fun setDragEnabled(enabled: Boolean)
    {
        longPressDragEnabled = enabled
    }

    fun setSwipeEnabled(enabled: Boolean)
    {
        itemSwipeEnabled = enabled
    }

}