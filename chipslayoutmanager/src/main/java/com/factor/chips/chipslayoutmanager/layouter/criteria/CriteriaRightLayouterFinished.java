package com.factor.chips.chipslayoutmanager.layouter.criteria;


import com.factor.chips.chipslayoutmanager.layouter.AbstractLayouter;

class CriteriaRightLayouterFinished implements IFinishingCriteria {

    @Override
    public boolean isFinishedLayouting(AbstractLayouter abstractLayouter) {
        return abstractLayouter.getViewLeft() >= abstractLayouter.getCanvasRightBorder();
    }
}
