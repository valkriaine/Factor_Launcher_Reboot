package com.factor.chips.chipslayoutmanager.layouter.criteria;


import androidx.annotation.NonNull;

public class InfiniteCriteriaFactory extends AbstractCriteriaFactory implements ICriteriaFactory {
    @NonNull
    @Override
    public IFinishingCriteria getBackwardFinishingCriteria() {
        return new InfiniteCriteria();
    }

    @NonNull
    @Override
    public IFinishingCriteria getForwardFinishingCriteria() {
        return new InfiniteCriteria();
    }
}
