package com.factor.chips.chipslayoutmanager.layouter.criteria;

import com.factor.chips.chipslayoutmanager.layouter.AbstractLayouter;

public class EmtpyCriteria implements IFinishingCriteria {
    @Override
    public boolean isFinishedLayouting(AbstractLayouter abstractLayouter) {
        return true;
    }

}
