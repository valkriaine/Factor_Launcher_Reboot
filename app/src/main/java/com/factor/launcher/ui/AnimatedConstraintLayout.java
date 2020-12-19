package com.factor.launcher.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;


public class AnimatedConstraintLayout extends ConstraintLayout
{

    private boolean isInDragAndDrop = false;

    @SuppressLint("ClickableViewAccessibility")
    public AnimatedConstraintLayout(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs)
    {
        super(context, attrs);

        this.setOnTouchListener((v, event) ->
        {
            switch(event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    if (!isInDragAndDrop)
                    {
                        animate().scaleX(0.95f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(50);
                        animate().scaleY(0.95f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(50);
                    }
                    return false; // if you want to handle the touch event
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (!isInDragAndDrop)
                    {
                        animate().scaleX(1f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(50);
                        animate().scaleY(1f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(50);
                    }
                    return false; // if you want to handle the touch event
            }
            return false;
        });
    }

    public void animateToSelectedState()
    {
        isInDragAndDrop = true;
        animate().scaleX(1.1f)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(200).start();
        animate().scaleY(1.1f)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(200).start();
    }

    public void animateToNormalState()
    {
        isInDragAndDrop = false;
        animate().scaleX(1f)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(200).start();
        animate().scaleY(1f)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(200).start();
    }


    @Override
    public boolean performClick()
    {
        return super.performClick();
    }
}


