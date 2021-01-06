package com.factor.chips.chipslayoutmanager;

interface IStateHolder {
    boolean isLayoutRTL();

    @Orientation
    int layoutOrientation();

}
