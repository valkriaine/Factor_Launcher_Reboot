package com.factor.chips.chipslayoutmanager.gravity;

import com.factor.chips.chipslayoutmanager.layouter.AbstractLayouter;
import com.factor.chips.chipslayoutmanager.layouter.Item;

import java.util.List;

public interface IRowStrategy {
    void applyStrategy(AbstractLayouter abstractLayouter, List<Item> row);
}
