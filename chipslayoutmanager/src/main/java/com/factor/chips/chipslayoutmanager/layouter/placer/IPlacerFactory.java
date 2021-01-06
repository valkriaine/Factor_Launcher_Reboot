package com.factor.chips.chipslayoutmanager.layouter.placer;

public interface IPlacerFactory {
    IPlacer getAtStartPlacer();
    IPlacer getAtEndPlacer();
}
