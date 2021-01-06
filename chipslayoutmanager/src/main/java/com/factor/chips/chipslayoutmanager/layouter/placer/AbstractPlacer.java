package com.factor.chips.chipslayoutmanager.layouter.placer;

import androidx.recyclerview.widget.RecyclerView;

abstract class AbstractPlacer implements IPlacer {

    private RecyclerView.LayoutManager layoutManager;

    AbstractPlacer(RecyclerView.LayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    public RecyclerView.LayoutManager getLayoutManager() {
        return layoutManager;
    }
}
