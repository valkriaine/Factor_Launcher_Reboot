package com.factor.launcher.ui.invisible_refresh_layout;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.animation.Animation;
import androidx.core.view.ViewCompat;

/**
 * Private class created to work around issues with AnimationListeners being
 * called before the animation is actually complete and support shadows on older
 * platforms.
 */
class InvisibleCircle extends androidx.appcompat.widget.AppCompatImageView
{
    private Animation.AnimationListener mListener;

    InvisibleCircle(Context context)
    {
        super(context);

        ShapeDrawable circle = new ShapeDrawable(new OvalShape());

        circle.getPaint().setColor(Color.TRANSPARENT);
        ViewCompat.setBackground(this, circle);
    }

    public void setAnimationListener(Animation.AnimationListener listener) {
        mListener = listener;
    }

    @Override
    public void onAnimationStart()
    {
        super.onAnimationStart();
        if (mListener != null) {
            mListener.onAnimationStart(getAnimation());
        }
    }

    @Override
    public void onAnimationEnd() {
        super.onAnimationEnd();
        if (mListener != null) {
            mListener.onAnimationEnd(getAnimation());
        }
    }
}

