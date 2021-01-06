package com.factor.chips.chipslayoutmanager.gravity;


import com.factor.chips.chipslayoutmanager.layouter.AbstractLayouter;

abstract class GravityUtil {

    static int getHorizontalDifference(AbstractLayouter layouter) {
        return layouter.getCanvasRightBorder() - layouter.getCanvasLeftBorder() - layouter.getRowLength();
    }

    static int getVerticalDifference(AbstractLayouter layouter) {
        return layouter.getCanvasBottomBorder() - layouter.getCanvasTopBorder() - layouter.getRowLength();
    }

}
