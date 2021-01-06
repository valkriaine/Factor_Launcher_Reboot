package com.factor.chips.chipslayoutmanager.layouter;

import androidx.recyclerview.widget.RecyclerView;
import com.factor.chips.chipslayoutmanager.gravity.IRowStrategyFactory;
import com.factor.chips.chipslayoutmanager.layouter.breaker.IBreakerFactory;

interface IOrientationStateFactory {
    ILayouterCreator createLayouterCreator(RecyclerView.LayoutManager lm);
    IRowStrategyFactory createRowStrategyFactory();
    IBreakerFactory createDefaultBreaker();
}
