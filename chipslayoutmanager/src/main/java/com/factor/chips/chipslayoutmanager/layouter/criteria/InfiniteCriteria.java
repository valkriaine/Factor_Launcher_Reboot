package com.factor.chips.chipslayoutmanager.layouter.criteria;


import com.factor.chips.chipslayoutmanager.layouter.AbstractLayouter;

class InfiniteCriteria implements IFinishingCriteria {

    @Override
    public boolean isFinishedLayouting(AbstractLayouter abstractLayouter) {
        return false;
    }

}
