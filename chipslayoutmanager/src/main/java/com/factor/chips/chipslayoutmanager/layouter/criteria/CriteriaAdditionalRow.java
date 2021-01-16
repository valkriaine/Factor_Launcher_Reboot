package com.factor.chips.chipslayoutmanager.layouter.criteria;


import com.factor.chips.chipslayoutmanager.layouter.AbstractLayouter;
import com.factor.chips.chipslayoutmanager.layouter.ILayouter;
import com.factor.chips.chipslayoutmanager.layouter.ILayouterListener;

class CriteriaAdditionalRow extends FinishingCriteriaDecorator implements IFinishingCriteria, ILayouterListener {

    private final int requiredRowsCount;

    private int additionalRowsCount;

    CriteriaAdditionalRow(IFinishingCriteria finishingCriteria, int requiredRowsCount) {
        super(finishingCriteria);
        this.requiredRowsCount = requiredRowsCount;
    }

    @Override
    public boolean isFinishedLayouting(AbstractLayouter abstractLayouter) {
        abstractLayouter.addLayouterListener(this);
        return super.isFinishedLayouting(abstractLayouter) && additionalRowsCount >= requiredRowsCount;
    }

    @Override
    public void onLayoutRow(ILayouter layouter) {
        if (super.isFinishedLayouting((AbstractLayouter) layouter)) {
            additionalRowsCount++;
        }
    }
}
