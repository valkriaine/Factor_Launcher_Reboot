package com.factor.chips.chipslayoutmanager.gravity;

import android.view.Gravity;
import com.factor.chips.chipslayoutmanager.SpanLayoutChildGravity;


public class CenterChildGravity implements IChildGravityResolver {
    @Override
    @SpanLayoutChildGravity
    public int getItemGravity(int position) {
        return Gravity.CENTER_VERTICAL;
    }
}
