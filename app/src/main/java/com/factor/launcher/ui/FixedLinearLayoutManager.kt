package com.factor.launcher.ui

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager


class FixedLinearLayoutManager(context: Context?) : LinearLayoutManager(context)
{
    override fun supportsPredictiveItemAnimations(): Boolean = false
}
