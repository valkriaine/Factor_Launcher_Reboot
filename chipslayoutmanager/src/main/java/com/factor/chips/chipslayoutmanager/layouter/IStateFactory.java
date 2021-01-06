package com.factor.chips.chipslayoutmanager.layouter;

import android.view.View;
import com.factor.chips.chipslayoutmanager.IScrollingController;
import com.factor.chips.chipslayoutmanager.anchor.AnchorViewState;
import com.factor.chips.chipslayoutmanager.anchor.IAnchorFactory;
import com.factor.chips.chipslayoutmanager.layouter.criteria.AbstractCriteriaFactory;
import com.factor.chips.chipslayoutmanager.layouter.criteria.ICriteriaFactory;
import com.factor.chips.chipslayoutmanager.layouter.placer.IPlacerFactory;


public interface IStateFactory {
    @SuppressWarnings("UnnecessaryLocalVariable")
    LayouterFactory createLayouterFactory(ICriteriaFactory criteriaFactory, IPlacerFactory placerFactory);

    AbstractCriteriaFactory createDefaultFinishingCriteriaFactory();

    IAnchorFactory anchorFactory();

    IScrollingController scrollingController();

    ICanvas createCanvas();

    int getSizeMode();

    int getStart();

    int getStart(View view);

    int getStart(AnchorViewState anchor);

    int getStartAfterPadding();

    int getStartViewPosition();

    int getStartViewBound();

    int getEnd();

    int getEnd(View view);

    int getEndAfterPadding();

    int getEnd(AnchorViewState anchor);

    int getEndViewPosition();

    int getEndViewBound();

    int getTotalSpace();
}
