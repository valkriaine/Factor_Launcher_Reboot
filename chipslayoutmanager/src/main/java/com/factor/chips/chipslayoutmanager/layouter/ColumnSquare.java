package com.factor.chips.chipslayoutmanager.layouter;


import androidx.recyclerview.widget.RecyclerView;

class ColumnSquare extends Square {

    ColumnSquare(RecyclerView.LayoutManager lm) {
        super(lm);
    }

    public final int getCanvasRightBorder() {
        return lm.getWidth();
    }

    public final int getCanvasBottomBorder() {
        return lm.getHeight() - lm.getPaddingBottom();
    }

    public final int getCanvasLeftBorder() {
        return 0;
    }

    public final int getCanvasTopBorder() {
        return lm.getPaddingTop();
    }

}
