package com.factor.chips.chipslayoutmanager.layouter.criteria;

import com.factor.chips.chipslayoutmanager.layouter.AbstractLayouter;

public interface IFinishingCriteria {
    /** check if layouting finished by criteria */
    boolean isFinishedLayouting(AbstractLayouter abstractLayouter);
}
