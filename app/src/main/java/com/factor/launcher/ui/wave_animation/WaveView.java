package com.factor.launcher.ui.wave_animation;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import com.factor.launcher.ui.ViewKt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


// https://github.com/ssynhtn/wave-view
public class WaveView extends View implements LifecycleOwner
{
    private static final String TAG = WaveView.class.getSimpleName();

    private static final float LINE_SMOOTHNESS = 0.16f;
    private static final boolean USE_ANIMATION = true;
    private Paint paint;

    private AnimatorSet animatorSet;
    private boolean animationPaused;

    @NonNull
    @Override
    public Lifecycle getLifecycle()
    {
        return ViewKt.getLifecycle(this);
    }

    public static class WaveData {

        public static final int GRADIENT_ORIENTATION_HORIZONTAL = 2;
        private static final int DEFAULT_SAMPLE_SIZE = 16;

        public WaveData(float waveLength, float waveHeight, float fixedHeight, float offset, int startColor, int endColor, float alpha, long duration, boolean right) {
            this.waveLength = waveLength;
            this.waveHeight = waveHeight;
            this.fixedHeight = fixedHeight;
            this.offset = offset;
            this.startColor = startColor;
            this.endColor = endColor;
            this.alpha = alpha;
            this.duration = duration;
            this.right = right;
        }

        /**
         * waveLength 波长
         * waveHeight 波高度
         * fixedHeight   波谷距离View的底部的固定高度, 相当于这个波的marginBottom
         * offset    波的初始位置
         * startColor    渐变色左边的颜色
         * endColor      渐变色右边的颜色
         * alpha     透明度
         * duration  波动周期, 走一个波长所需要的时间
         * right 是否向右运动
         * sampleSize 用path绘制正弦曲线, 一个周期取的样点
         * gradientOrientation 渐变色方向
         *
         */

        private float waveLength;
        private float waveHeight;
        private float fixedHeight;
        private float offset;
        private long duration;
        private int startColor;
        private int endColor;
        private float alpha;
        private boolean right;
        private int sampleSize = DEFAULT_SAMPLE_SIZE;
        private int gradientOrientation = GRADIENT_ORIENTATION_HORIZONTAL;

        private Shader shader;
        private ValueAnimator valueAnimator;
        private final Path path = new Path();

        private float lengthScale = 1;
        private float heightScale = 1;
        private float durationScale = 1;
        private float fixedHeightScale = 1;

        public float getWaveLength() {
            return waveLength * lengthScale;
        }

        public float getWaveHeight() {
            return waveHeight * heightScale;
        }

        public long getDuration() {
            return (long) (duration * durationScale);
        }

        public float getFixedHeight() {
            return fixedHeight * fixedHeightScale;
        }

        public WaveData setWaveLength(float waveLength) {
            this.waveLength = waveLength;
            return this;
        }

        public WaveData setWaveHeight(float waveHeight) {
            this.waveHeight = waveHeight;
            return this;
        }

        public WaveData setFixedHeight(float fixedHeight) {
            this.fixedHeight = fixedHeight;
            return this;
        }

        public WaveData setOffset(float offset) {
            this.offset = offset;
            return this;
        }

        public WaveData setDuration(long duration) {
            this.duration = duration;
            return this;
        }

        public WaveData setStartColor(int startColor) {
            this.startColor = startColor;
            return this;
        }

        public WaveData setEndColor(int endColor) {
            this.endColor = endColor;
            return this;
        }

        public WaveData setAlpha(float alpha) {
            this.alpha = alpha;
            return this;
        }

        public WaveData setRight(boolean right) {
            this.right = right;
            return this;
        }

        public WaveData setSampleSize(int sampleSize) {
            this.sampleSize = sampleSize;
            return this;
        }

        public WaveData setGradientOrientation(int gradientOrientation) {
            this.gradientOrientation = gradientOrientation;
            return this;
        }
    }


    private final List<WaveData> waveDataList = new ArrayList<>();

    public WaveView(Context context) {
        super(context);
        init(null, 0);
    }

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public WaveView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    }

    public void startAnimation() {
        if (USE_ANIMATION) {
            if (animatorSet != null) return;

            animatorSet = new AnimatorSet();

            List<Animator> animators = new ArrayList<>();
            boolean first = true;
            for (WaveData waveData : waveDataList) {
                ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
                animator.setDuration(waveData.getDuration());
                animator.setInterpolator(new LinearInterpolator());
                animator.setRepeatMode(ValueAnimator.RESTART);
                animator.setRepeatCount(ValueAnimator.INFINITE);
                if (first) {
                    animator.addUpdateListener(valueAnimator ->
                    {
//                            Log.d(TAG, "onAnimationUpdate");
                        ViewCompat.postInvalidateOnAnimation(WaveView.this);
                    });
                }
                first = false;
                waveData.valueAnimator = animator;
                animators.add(animator);
            }

            animatorSet.playTogether(animators);
            animatorSet.start();
        }
    }

    public boolean isAnimationStarted() {
        return animatorSet != null && animatorSet.isStarted();
    }

    public boolean isAnimationPaused() {
        return isAnimationStarted() && animationPaused;
    }

    public void pauseAnimation() {
        if (animatorSet == null || !animatorSet.isStarted()) return;

        if (animationPaused) return;

        animationPaused = true;
        if (animatorSet != null) {
            animatorSet.pause();
        }
    }

    public void resumeAnimation() {
        if (animatorSet == null || !animatorSet.isStarted()) return;

        if (animationPaused) {
            animationPaused = false;
            if (animatorSet != null) {
                animatorSet.resume();
            }
        }
    }

    public void stopAnimation()
    {
        pauseAnimation();
        onDetachedFromWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (animatorSet != null) {
            animatorSet.end();
            animatorSet = null;

            for (WaveData waveData : waveDataList) {
                waveData.valueAnimator = null;
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxHeight = 0;
        for (WaveData waveData : waveDataList) {
            maxHeight = (int) Math.max(maxHeight, waveData.getFixedHeight() + waveData.getWaveHeight());
        }

        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), resolveSize(maxHeight, heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        for (WaveData waveData : waveDataList) {
            resetPath(waveData, w, h);
        }
    }

    private void resetPath(WaveData waveData, int w, int h)
    {
        if (waveData.gradientOrientation == WaveData.GRADIENT_ORIENTATION_HORIZONTAL)
        {
            waveData.shader = new LinearGradient(0, 0, w, 0, waveData.startColor, waveData.endColor, Shader.TileMode.REPEAT);
        }
        else
        {
            waveData.shader = new LinearGradient(0, h - waveData.getFixedHeight() - waveData.getWaveHeight(), 0, h - waveData.fixedHeight, waveData.startColor, waveData.endColor, Shader.TileMode.REPEAT);
        }

        float cy = h - waveData.getFixedHeight() - waveData.getWaveHeight()/ 2;
        setSineWave(w, h, cy, waveData, waveData.sampleSize);
    }

    private void setSineWave(int w, int h, float cy, WaveData waveData, int sampleCount) {
        Path path = waveData.path;
        path.reset();

        float waveLength = waveData.getWaveLength();
        float waveHeight = waveData.getWaveHeight();

        float offMax = waveData.offset + waveLength;
        float offMin = waveData.offset - waveLength;
        float left = -offMax;
        float right = w - offMin;
        float x = left;
        float delta = waveData.getWaveLength() / sampleCount;

        float prePreviousPointX = x - delta * 2;
        float prePreviousPointY = computeY(prePreviousPointX, left, waveLength, waveHeight, cy, waveData);
        float previousPointX = x - delta;
        float previousPointY = computeY(previousPointX, left, waveLength, waveHeight, cy, waveData);
        float currentPointX = x;
        float currentPointY = computeY(currentPointX, left, waveLength, waveHeight, cy, waveData);
        float nextPointX = x + delta;
        float nextPointY = computeY(nextPointX, left, waveLength, waveHeight, cy, waveData);

        boolean first = true;
        while (x - delta < right){
            if (first) {
                // Move to start point.
                path.moveTo(currentPointX, currentPointY);
                first = false;
            } else {
                // Calculate control points.
                final float firstDiffX = (currentPointX - prePreviousPointX);
                final float firstDiffY = (currentPointY - prePreviousPointY);
                final float secondDiffX = (nextPointX - previousPointX);
                final float secondDiffY = (nextPointY - previousPointY);
                final float firstControlPointX = previousPointX + (LINE_SMOOTHNESS * firstDiffX);
                final float firstControlPointY = previousPointY + (LINE_SMOOTHNESS * firstDiffY);
                final float secondControlPointX = currentPointX - (LINE_SMOOTHNESS * secondDiffX);
                final float secondControlPointY = currentPointY - (LINE_SMOOTHNESS * secondDiffY);
                path.cubicTo(firstControlPointX, firstControlPointY, secondControlPointX, secondControlPointY,
                        currentPointX, currentPointY);
//                Log.d(TAG, "cubic to " + currentPointX + ", " + currentPointY);
            }

            // Shift values by one back to prevent recalculation of values that have
            // been already calculated.
            prePreviousPointX = previousPointX;
            prePreviousPointY = previousPointY;
            previousPointX = currentPointX;
            previousPointY = currentPointY;
            currentPointX = nextPointX;
            currentPointY = nextPointY;
            nextPointX += delta;
            nextPointY = computeY(nextPointX, left, waveLength, waveHeight, cy, waveData);

            x = x + delta;
        }

        path.lineTo(x, h);
        path.lineTo(left, h);

        path.close();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (WaveData waveData : waveDataList)
        {
            drawWave(canvas, waveData);
        }
    }

    private final Matrix matrix = new Matrix();
    private void drawWave(Canvas canvas, WaveData waveData)
    {
        if (waveData == null)
            return;

        float translation = USE_ANIMATION && waveData.valueAnimator != null ? waveData.valueAnimator.getAnimatedFraction() * waveData.getWaveLength() * (waveData.right ? 1 : -1) + waveData.offset : waveData.offset;

        canvas.save();
        canvas.translate(translation, 0);
        matrix.setTranslate(-translation, 0);

        if (waveData.shader == null)
            resetPath(waveData, getWidth(), getHeight());

        waveData.shader.setLocalMatrix(matrix);

        paint.setAlpha((int) (waveData.alpha * 255));
        paint.setShader(waveData.shader);

        canvas.drawPath(waveData.path, paint);
        canvas.restore();

    }



    private float computeY(float x, float startX, float waveLength, float waveHeight, float cy, WaveData waveData) {
        float cycle = (x - waveData.offset - startX) / waveLength;
        return (cy - waveHeight / 2 * sineWaveShapeFunction((float) (cycle * Math.PI * 2)));
    }

    private float sineWaveShapeFunction(float x) {
        return (float) Math.sin(x);
    }


    public void addWaveData(WaveData... waveData) {
        Collections.addAll(waveDataList, waveData);

        requestLayout();
    }

    public void updateWaveLength(int i, float lengthScale) {
        if (i >= 0 && i < waveDataList.size()) {
            WaveData waveData = waveDataList.get(i);
            waveData.lengthScale = lengthScale;

            int width = getWidth();
            int height = getHeight();

            if (width > 0 && height > 0) {
                resetPath(waveData, width, height);
            }
        }
    }

    public void updateWaveHeight(int i , float heightScale) {
        if (i >= 0 && i < waveDataList.size()) {
            final WaveData waveData = waveDataList.get(i);
            waveData.heightScale = heightScale;

            requestLayout();
            post(() -> {
                int width = getWidth();
                int height = getHeight();
                if (width > 0 && height > 0) {
                    resetPath(waveData, width, height);
                }
            });
        }
    }

    public void updateWaveDuration(int i, float durationScale) {
        if (i >= 0 && i < waveDataList.size()) {
            WaveData waveData = waveDataList.get(i);
            waveData.durationScale = durationScale;
            waveData.valueAnimator.setDuration(waveData.getDuration());
        }
    }

    public void updateWaveFixedHeight(int i, float scale) {
        if (i >= 0 && i < waveDataList.size()) {
            final WaveData waveData = waveDataList.get(i);
            waveData.fixedHeightScale = scale;

            requestLayout();
            post(() -> {
                int width = getWidth();
                int height = getHeight();
                if (width > 0 && height > 0) {
                    resetPath(waveData, width, height);
                }
            });
        }
    }
}
