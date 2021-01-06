package com.factor.chips.chipslayoutmanager.layouter.placer;

import androidx.recyclerview.widget.RecyclerView;

class DisappearingPlacerFactory implements IPlacerFactory {

    private final RecyclerView.LayoutManager layoutManager;

    DisappearingPlacerFactory(RecyclerView.LayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    @Override
    public IPlacer getAtStartPlacer() {
        return new DisappearingViewAtStartPlacer(layoutManager);
    }

    @Override
    public IPlacer getAtEndPlacer() {
        return new DisappearingViewAtEndPlacer(layoutManager);
    }
}
