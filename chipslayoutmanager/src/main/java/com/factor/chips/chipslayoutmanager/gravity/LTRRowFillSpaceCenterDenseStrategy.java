package com.factor.chips.chipslayoutmanager.gravity;

import android.graphics.Rect;
import com.factor.chips.chipslayoutmanager.layouter.AbstractLayouter;
import com.factor.chips.chipslayoutmanager.layouter.Item;


import java.util.List;

class LTRRowFillSpaceCenterDenseStrategy implements IRowStrategy {

    @Override
    public void applyStrategy(AbstractLayouter abstractLayouter, List<Item> row) {
        int difference = GravityUtil.getHorizontalDifference(abstractLayouter) / 2;

        for (Item item : row) {
            Rect childRect = item.getViewRect();
            childRect.left += difference;
            childRect.right += difference;
        }
    }
}
