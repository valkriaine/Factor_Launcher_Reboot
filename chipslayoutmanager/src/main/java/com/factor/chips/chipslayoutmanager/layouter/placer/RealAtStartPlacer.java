package com.factor.chips.chipslayoutmanager.layouter.placer;

import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

class RealAtStartPlacer extends AbstractPlacer implements IPlacer {
    RealAtStartPlacer(RecyclerView.LayoutManager layoutManager) {
        super(layoutManager);
    }

    @Override
    public void addView(View view) {
        //mark that we add view at beginning of children
        getLayoutManager().addView(view, 0);
    }
}
