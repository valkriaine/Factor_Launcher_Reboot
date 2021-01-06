package com.factor.chips.chipslayoutmanager.layouter.placer;


import com.factor.chips.chipslayoutmanager.ChipsLayoutManager;

public class PlacerFactory {

    private final ChipsLayoutManager lm;

    public PlacerFactory(ChipsLayoutManager lm) {
        this.lm = lm;
    }

    public IPlacerFactory createRealPlacerFactory() {
        return new RealPlacerFactory(lm);
    }

    public IPlacerFactory createDisappearingPlacerFactory() {
        return new DisappearingPlacerFactory(lm);
    }

}
