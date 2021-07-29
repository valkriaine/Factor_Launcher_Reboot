package com.factor.launcher.ui.invisible_refresh_layout;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.View;
import android.view.animation.Animation;
import androidx.core.view.ViewCompat;

/**
 * Private class created to work around issues with AnimationListeners being
 * called before the animation is actually complete and support shadows on older
 * platforms.
 */
class InvisibleCircleImageView extends androidx.appcompat.widget.AppCompatImageView {

    private static final int FILL_SHADOW_COLOR = Color.TRANSPARENT;
    private static final int KEY_SHADOW_COLOR = Color.TRANSPARENT;

    // PX
    private static final float X_OFFSET = 0f;
    private static final float Y_OFFSET = 1.75f;


    private Animation.AnimationListener mListener;
    private int mBackgroundColor;

    InvisibleCircleImageView(Context context) {
        super(context);

        final float density = getContext().getResources().getDisplayMetrics().density;
        final int shadowYOffset = (int) (density * Y_OFFSET);
        final int shadowXOffset = (int) (density * X_OFFSET);



        mBackgroundColor = Color.TRANSPARENT;


        ShapeDrawable circle;
        if (elevationSupported()) {
            circle = new ShapeDrawable(new OvalShape());

        } else {
            circle = new ShapeDrawable(new InvisibleCircleImageView.OvalShadow(this));
            setLayerType(View.LAYER_TYPE_SOFTWARE, circle.getPaint());
            circle.getPaint().setShadowLayer(0, shadowXOffset, shadowYOffset,
                    KEY_SHADOW_COLOR);

        }
        circle.getPaint().setColor(mBackgroundColor);
        ViewCompat.setBackground(this, circle);
    }

    private boolean elevationSupported() {
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!elevationSupported()) {
            setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
        }
    }

    public void setAnimationListener(Animation.AnimationListener listener) {
        mListener = listener;
    }

    @Override
    public void onAnimationStart() {
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

    @Override
    public void setBackgroundColor(int color) {
        if (getBackground() instanceof ShapeDrawable) {
            ((ShapeDrawable) getBackground()).getPaint().setColor(color);
            mBackgroundColor = color;
        }
    }

    private static class OvalShadow extends OvalShape {
        private final Paint mShadowPaint;
        private final InvisibleCircleImageView mCircleImageView;

        OvalShadow(InvisibleCircleImageView circleImageView) {
            super();
            mCircleImageView = circleImageView;
            mShadowPaint = new Paint();
            updateRadialGradient();
        }

        @Override
        protected void onResize(float width, float height) {
            super.onResize(width, height);
            updateRadialGradient();
        }

        @Override
        public void draw(Canvas canvas, Paint paint) {
            final int x = mCircleImageView.getWidth() / 2;
            final int y = mCircleImageView.getHeight() / 2;
            canvas.drawCircle(x, y, x, mShadowPaint);
            canvas.drawCircle(x, y, x, paint);
        }

        private void updateRadialGradient() {
            mShadowPaint.setShader(new RadialGradient(
                    0,
                    0,
                    0,
                    new int[]{FILL_SHADOW_COLOR, Color.TRANSPARENT},
                    null,
                    Shader.TileMode.CLAMP));
        }
    }
}

