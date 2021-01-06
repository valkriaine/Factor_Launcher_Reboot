package com.factor.chips.chipslayoutmanager.gravity;


import com.factor.chips.chipslayoutmanager.RowStrategy;

public interface IRowStrategyFactory {
    IRowStrategy createRowStrategy(@RowStrategy int rowStrategy);
}
