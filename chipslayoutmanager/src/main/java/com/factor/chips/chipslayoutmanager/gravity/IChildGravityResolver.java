package com.factor.chips.chipslayoutmanager.gravity;


import com.factor.chips.chipslayoutmanager.SpanLayoutChildGravity;

/** class which determines child gravity inside row from child position */
public interface IChildGravityResolver {
    @SpanLayoutChildGravity
    int getItemGravity(int position);
}
