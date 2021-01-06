package com.factor.chips.chipslayoutmanager.layouter.placer;

import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

class RealAtEndPlacer extends AbstractPlacer implements IPlacer {
    RealAtEndPlacer(RecyclerView.LayoutManager layoutManager) {
        super(layoutManager);
    }

    @Override
    public void addView(View view) {
        getLayoutManager().addView(view);

//        Log.i("added view, position = " + getLayoutManager().getPosition(view));
    }
}
