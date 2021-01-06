package com.factor.chips.chipslayoutmanager.layouter.placer;

import androidx.recyclerview.widget.RecyclerView;

class RealPlacerFactory implements IPlacerFactory {

    private final RecyclerView.LayoutManager layoutManager;

    RealPlacerFactory(RecyclerView.LayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    @Override
    public IPlacer getAtStartPlacer() {
        return new RealAtStartPlacer(layoutManager);
    }

    @Override
    public IPlacer getAtEndPlacer() {
        return new RealAtEndPlacer(layoutManager);
    }
}
