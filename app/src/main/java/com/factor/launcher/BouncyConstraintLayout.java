package com.factor.launcher;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

/**
 * TODO: animation does not work yet.
 */
public class BouncyConstraintLayout extends ConstraintLayout
{

    private final SpringAnimation animationY;
    private final SpringAnimation animationX;


    public BouncyConstraintLayout(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs)
    {
        super(context, attrs);
        animationY = new SpringAnimation(this, DynamicAnimation.SCALE_Y);
        animationX = new SpringAnimation(this, DynamicAnimation.SCALE_X);

        animationX.setSpring(new SpringForce()
                .setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY)
                .setStiffness(SpringForce.STIFFNESS_LOW));

        animationY.setSpring(new SpringForce()
                .setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY)
                .setStiffness(SpringForce.STIFFNESS_LOW));
    }


    @Override
    public void setScaleX(float scaleX)
    {
        animationX.getSpring().setFinalPosition(scaleX);
        animationX.start();
    }

    @Override
    public void setScaleY(float scaleY)
    {
        animationY.getSpring().setFinalPosition(scaleY);
        animationY.start();
    }
}


