package com.factor.chips.chipslayoutmanager.layouter;

import android.graphics.Rect;
import androidx.recyclerview.widget.RecyclerView;
import com.factor.chips.chipslayoutmanager.anchor.AnchorViewState;


class LTRRowsCreator implements ILayouterCreator {

    private RecyclerView.LayoutManager layoutManager;

    LTRRowsCreator(RecyclerView.LayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    @Override
    public Rect createOffsetRectForBackwardLayouter(AnchorViewState anchor) {
        Rect anchorRect = anchor.getAnchorViewRect();

        return new Rect(
                0,
                anchorRect == null ? 0 : anchorRect.top,
                //we shouldn't include anchor view here, so anchorLeft is a rightOffset
                anchorRect == null ? 0 : anchorRect.left,
                anchorRect == null ? 0 : anchorRect.bottom);
    }

    @Override
    public Rect createOffsetRectForForwardLayouter(AnchorViewState anchor) {
        Rect anchorRect = anchor.getAnchorViewRect();

        return new Rect(
                //we should include anchor view here, so anchorLeft is a leftOffset
                anchorRect == null ? layoutManager.getPaddingLeft() : anchorRect.left,
                anchorRect == null ? anchor.getPosition() == 0 ? layoutManager.getPaddingTop() : 0 : anchorRect.top,
                0,
                anchorRect == null ? anchor.getPosition() == 0 ? layoutManager.getPaddingBottom() : 0 : anchorRect.bottom);
    }

    @Override
    public AbstractLayouter.Builder createBackwardBuilder() {
        return LTRUpLayouter.newBuilder();
    }

    @Override
    public AbstractLayouter.Builder createForwardBuilder() {
        return LTRDownLayouter.newBuilder();
    }
}
