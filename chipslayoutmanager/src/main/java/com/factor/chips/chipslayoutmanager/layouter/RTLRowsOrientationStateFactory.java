package com.factor.chips.chipslayoutmanager.layouter;

import androidx.recyclerview.widget.RecyclerView;
import com.factor.chips.chipslayoutmanager.gravity.IRowStrategyFactory;
import com.factor.chips.chipslayoutmanager.gravity.RTLRowStrategyFactory;
import com.factor.chips.chipslayoutmanager.layouter.breaker.IBreakerFactory;
import com.factor.chips.chipslayoutmanager.layouter.breaker.RTLRowBreakerFactory;

class RTLRowsOrientationStateFactory implements IOrientationStateFactory {

    @Override
    public ILayouterCreator createLayouterCreator(RecyclerView.LayoutManager lm) {
        return new RTLRowsCreator(lm);
    }

    @Override
    public IRowStrategyFactory createRowStrategyFactory() {
        return new RTLRowStrategyFactory();
    }

    @Override
    public IBreakerFactory createDefaultBreaker() {
        return new RTLRowBreakerFactory();
    }
}
