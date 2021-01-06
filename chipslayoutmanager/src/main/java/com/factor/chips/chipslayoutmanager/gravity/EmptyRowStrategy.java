package com.factor.chips.chipslayoutmanager.gravity;


import com.factor.chips.chipslayoutmanager.layouter.AbstractLayouter;
import com.factor.chips.chipslayoutmanager.layouter.Item;

import java.util.List;

class EmptyRowStrategy implements IRowStrategy {
    @Override
    public void applyStrategy(AbstractLayouter abstractLayouter, List<Item> row) {
        //do nothing
    }
}
