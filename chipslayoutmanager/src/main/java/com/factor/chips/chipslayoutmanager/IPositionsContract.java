package com.factor.chips.chipslayoutmanager;

interface IPositionsContract {
    int findFirstVisibleItemPosition();
    int findFirstCompletelyVisibleItemPosition();
    int findLastVisibleItemPosition();
    int findLastCompletelyVisibleItemPosition();
}
