package com.factor.chips.chipslayoutmanager.layouter;

import androidx.recyclerview.widget.RecyclerView;
import com.factor.chips.chipslayoutmanager.gravity.IRowStrategyFactory;
import com.factor.chips.chipslayoutmanager.gravity.LTRRowStrategyFactory;
import com.factor.chips.chipslayoutmanager.layouter.breaker.IBreakerFactory;
import com.factor.chips.chipslayoutmanager.layouter.breaker.LTRRowBreakerFactory;

class LTRRowsOrientationStateFactory implements IOrientationStateFactory {

    @Override
    public ILayouterCreator createLayouterCreator(RecyclerView.LayoutManager lm) {
        return new LTRRowsCreator(lm);
    }

    @Override
    public IRowStrategyFactory createRowStrategyFactory() {
        return new LTRRowStrategyFactory();
    }

    @Override
    public IBreakerFactory createDefaultBreaker() {
        return new LTRRowBreakerFactory();
    }
}
