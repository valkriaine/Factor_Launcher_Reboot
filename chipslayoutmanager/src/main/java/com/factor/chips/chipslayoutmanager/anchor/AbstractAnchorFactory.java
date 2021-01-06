package com.factor.chips.chipslayoutmanager.anchor;


import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import com.factor.chips.chipslayoutmanager.layouter.ICanvas;

abstract class AbstractAnchorFactory implements IAnchorFactory {
    RecyclerView.LayoutManager lm;
    private final ICanvas canvas;

    AbstractAnchorFactory(RecyclerView.LayoutManager lm, ICanvas canvas) {
        this.lm = lm;
        this.canvas = canvas;
    }

    ICanvas getCanvas() {
        return canvas;
    }

    AnchorViewState createAnchorState(View view) {
        return new AnchorViewState(lm.getPosition(view), canvas.getViewRect(view));
    }

    @Override
    public AnchorViewState createNotFound() {
        return AnchorViewState.getNotFoundState();
    }

}
