package com.factor.chips.chipslayoutmanager.gravity;

import android.graphics.Rect;
import com.factor.chips.chipslayoutmanager.layouter.AbstractLayouter;
import com.factor.chips.chipslayoutmanager.layouter.Item;

import java.util.List;

class ColumnFillSpaceCenterDenseStrategy implements IRowStrategy
{

    @Override
    public void applyStrategy(AbstractLayouter abstractLayouter, List<Item> row)
    {
        int difference = GravityUtil.getVerticalDifference(abstractLayouter) / 2;

        for (Item item : row) {
            Rect childRect = item.getViewRect();
            childRect.top += difference;
            childRect.bottom += difference;
        }
    }
}
