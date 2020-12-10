package com.factor.launcher.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * TODO: make better animation
 */
public class BouncyConstraintLayout extends ConstraintLayout
{
    public BouncyConstraintLayout(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs)
    {
        super(context, attrs);
    }


    @Override
    public void setScaleX(float scaleX)
    {
        animate().scaleX(scaleX)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(200);
    }

    @Override
    public void setScaleY(float scaleY)
    {
        animate().scaleY(scaleY)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(200);
    }
}


