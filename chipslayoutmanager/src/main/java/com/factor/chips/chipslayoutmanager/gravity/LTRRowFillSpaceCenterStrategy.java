package com.factor.chips.chipslayoutmanager.gravity;

import android.graphics.Rect;
import com.factor.chips.chipslayoutmanager.layouter.AbstractLayouter;
import com.factor.chips.chipslayoutmanager.layouter.Item;
import java.util.List;

class LTRRowFillSpaceCenterStrategy implements IRowStrategy {

    @Override
    public void applyStrategy(AbstractLayouter abstractLayouter, List<Item> row) {
        int difference = GravityUtil.getHorizontalDifference(abstractLayouter) / (abstractLayouter.getRowSize() + 1);
        int offsetDifference = 0;

        for (Item item : row) {
            Rect childRect = item.getViewRect();

            offsetDifference += difference;

            childRect.left += offsetDifference;
            childRect.right += offsetDifference;
        }
    }
}
