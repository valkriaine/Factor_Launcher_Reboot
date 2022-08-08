package com.factor.launcher.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import com.factor.launcher.R;
import com.factor.launcher.models.AppSettings;
import com.factor.launcher.models.Factor;
import com.factor.launcher.ui.ElevationImageView;
import com.factor.launcher.ui.ViewKt;
import com.factor.launcher.ui.wave_animation.WaveView;
import com.factor.launcher.ui.wave_animation.Waves;
import com.factor.launcher.util.Util;
import com.google.android.material.card.MaterialCardView;
import eightbitlab.com.blurview.*;

import static androidx.core.app.NotificationCompat.CATEGORY_TRANSPORT;

/**
 * Medium tile
 */
public class FactorMediumView extends ConstraintLayout implements LifecycleOwner
{
    private boolean isMediaTile = false;
    private int notificationState = 0; //0 for no notification
    private BlurView trans;

    private MaterialCardView card;

    private ElevationImageView tileIcon;

    private AppCompatButton notificationCount;

    private AppCompatTextView tileLabel;

    private AppCompatTextView notificationTitle;

    private AppCompatTextView notificationContent;

    private Guideline notificationStart;

    private Guideline notificationEnd;

    private Guideline guidelineTitleTop;

    private Guideline guidelineTitleBottom;

    private Guideline guidelineContentBottom;


    private Guideline guidelineTitleEnd;


    private WaveView waveView;


    private final WaveView.WaveData wave1 = Waves.generateWave();

    private final WaveView.WaveData wave2 = Waves.generateWave();

    private final WaveView.WaveData wave3 = Waves.generateWave();

    public FactorMediumView(Context context) {
        super(context);
        init();
    }

    public FactorMediumView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FactorMediumView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public WaveView getWaveView()
    {
        return waveView;
    }


    private void init()
    {
        View.inflate(getContext(), R.layout.factor_medium_view, this);
        trans = findViewById(R.id.trans);
        card = findViewById(R.id.card);
        tileLabel = findViewById(R.id.tileLabel);
        tileIcon = findViewById(R.id.tileIcon);
        notificationCount = findViewById(R.id.notification_count);

        notificationTitle = findViewById(R.id.notification_title);
        notificationContent = findViewById(R.id.notification_content);
        guidelineTitleTop = findViewById(R.id.guideline_title_top);
        guidelineTitleBottom = findViewById(R.id.guideline_title_bottom);
        guidelineContentBottom = findViewById(R.id.guideline_content_bottom);
        guidelineTitleEnd = findViewById(R.id.guideline_notification_title_end);

        notificationStart = findViewById(R.id.guideline_notification_content_start);
        notificationEnd = findViewById(R.id.guideline_notification_content_end);

        waveView = findViewById(R.id.wave);
        waveView.setEnabled(false);
    }

    public void setupWaves(Factor factor)
    {
        wave1.setStartColor(factor.getUserApp().getVibrantColor());
        wave1.setEndColor(factor.getUserApp().getDominantColor());

        wave2.setStartColor(factor.getUserApp().getDominantColor());
        wave2.setEndColor(factor.getUserApp().getDarkMutedColor());

        wave3.setStartColor(factor.getUserApp().getVibrantColor());
        wave3.setEndColor(factor.getUserApp().getDarkMutedColor());

        waveView.addWaveData(wave1);
        waveView.addWaveData(wave2);
        waveView.addWaveData(wave3);

        waveView.setEnabled(true);
        isMediaTile = true;
    }


    // sound wave animation for media notification
    public void startWave()
    {
        if (waveView.getAlpha() != 1)
            waveView.animate().alpha(1).setDuration(150).start();

        waveView.startAnimation();

    }

    public void stopWave()
    {
        waveView.pauseAnimation();
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        if (waveView.isEnabled() && isMediaTile)
            waveView.startAnimation();
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        if (waveView.isEnabled())
            stopWave();
    }

    private void setUpNotificationCount(String count)
    {
        notificationCount.setVisibility(count.equals("0") ? INVISIBLE : VISIBLE);
        notificationCount.setText(count);
    }

    public void setupTile(AppSettings appSettings, boolean isLiveWallpaper, ViewGroup background)
    {
        //label color
        tileLabel.setTextColor(appSettings.isDarkText() ? Color.BLACK : Color.WHITE);
        notificationTitle.setTextColor(appSettings.isDarkText() ? Color.BLACK : Color.WHITE);
        notificationContent.setTextColor(appSettings.isDarkText() ? Color.BLACK : Color.WHITE);

        //initialize blur and color
        if (isLiveWallpaper || !appSettings.isBlurred() || appSettings.getStaticBlur())
        {
            trans.setVisibility(INVISIBLE);
            card.setCardBackgroundColor(Color.parseColor("#" + appSettings.getTileThemeColor()));
        }
        else
        {
            trans.setVisibility(VISIBLE);
            card.setCardBackgroundColor(Color.TRANSPARENT);

            BlurAlgorithm algorithm;

            algorithm = new RenderScriptBlur(getContext());

            trans.setupWith(background, algorithm)
                    .setOverlayColor(Color.parseColor("#" + appSettings.getTileThemeColor()))
                    .setBlurRadius(appSettings.getBlurRadius())
                    .setBlurAutoUpdate(false);
        }

        card.setRadius(Util.dpToPx(appSettings.getCornerRadius(), getContext()));
        tileIcon.setElevationDp(appSettings.getShowShadowAroundIcon()? 50 : 0);
    }

    public void setupContent(Factor factor)
    {

        setUpNotificationCount(factor.retrieveNotificationCount());
        isMediaTile = factor.getUserApp().isMediaTile();
        notificationTitle.setText(factor.getUserApp().getNotificationTitle());
        notificationContent.setText(factor.getUserApp().getNotificationText());

        tileLabel.setText(factor.getLabelNew());
        if (factor.getIcon() != null)
            tileIcon.setImageDrawable(factor.getIcon());
        updateLayout(factor);
    }

    private void updateLayout(Factor factor)
    {
        int newCount = factor.getNotificationCount();
        if (newCount != notificationState)
        {

            if (notificationState == 0)
            {
                // animate new notification arrived
                ValueAnimator animatorNotificationStart = ValueAnimator.ofFloat(0.4f, 0.05f);
                animatorNotificationStart.setDuration(300);
                animatorNotificationStart.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorNotificationStart.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams)notificationStart.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    notificationStart.setLayoutParams(lp);

                });

                ValueAnimator animatorNotificationEnd = ValueAnimator.ofFloat(0.97f, 0.95f);
                animatorNotificationEnd.setDuration(300);
                animatorNotificationEnd.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorNotificationEnd.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams)notificationEnd.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    notificationEnd.setLayoutParams(lp);

                });

                ValueAnimator animatorTitleTop = ValueAnimator.ofFloat(0.27f, 0.05f);
                animatorTitleTop.setDuration(300);
                animatorTitleTop.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorTitleTop.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams) guidelineTitleTop.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    guidelineTitleTop.setLayoutParams(lp);

                });

                ValueAnimator animatorTitleBottom = ValueAnimator.ofFloat(0.5f, 0.27f);
                animatorTitleBottom.setDuration(300);
                animatorTitleBottom.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorTitleBottom.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams) guidelineTitleBottom.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    guidelineTitleBottom.setLayoutParams(lp);

                });

                ValueAnimator animatorContentBottom = ValueAnimator.ofFloat(0.73f, 0.85f);
                animatorContentBottom.setDuration(300);
                animatorContentBottom.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorContentBottom.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams) guidelineContentBottom.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    guidelineContentBottom.setLayoutParams(lp);

                });

                ValueAnimator animatorTitleEnd = ValueAnimator.ofFloat(0.975f, 0.75f);
                animatorTitleEnd.setDuration(300);
                animatorTitleEnd.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorTitleEnd.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams) guidelineTitleEnd.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    guidelineTitleEnd.setLayoutParams(lp);

                });


                animatorNotificationStart.start();
                animatorNotificationEnd.start();
                animatorTitleTop.start();
                animatorTitleBottom.start();
                animatorContentBottom.start();
                animatorTitleEnd.start();


                tileIcon.animate().translationX(-500f).setDuration(400).start();

                notificationContent.setLines(3);
                notificationTitle.setGravity(Gravity.NO_GRAVITY);
                notificationContent.setGravity(Gravity.NO_GRAVITY);



                if (factor.getUserApp().getNotificationCategory() != null
                        && factor.getUserApp().getNotificationCategory() .equals(CATEGORY_TRANSPORT) && factor.getUserApp().isMediaTile())
                {
                    setupWaves(factor);
                    startWave();
                }


            }
            else if (newCount == 0)
            {
                // animate back to normal layout
                ValueAnimator animatorNotificationStart = ValueAnimator.ofFloat(0.05f, 0.4f).setDuration(300);
                animatorNotificationStart.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorNotificationStart.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams)notificationStart.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    notificationStart.setLayoutParams(lp);

                });

                ValueAnimator animatorNotificationEnd = ValueAnimator.ofFloat(0.95f, 0.97f).setDuration(300);
                animatorNotificationEnd.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorNotificationEnd.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams)notificationEnd.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    notificationEnd.setLayoutParams(lp);

                });

                ValueAnimator animatorTitleTop = ValueAnimator.ofFloat(0.05f, 0.27f).setDuration(300);
                animatorTitleTop.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorTitleTop.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams) guidelineTitleTop.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    guidelineTitleTop.setLayoutParams(lp);

                });

                ValueAnimator animatorTitleBottom = ValueAnimator.ofFloat(0.27f, 0.5f).setDuration(300);
                animatorTitleBottom.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorTitleBottom.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams) guidelineTitleBottom.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    guidelineTitleBottom.setLayoutParams(lp);

                });

                ValueAnimator animatorContentBottom = ValueAnimator.ofFloat(0.85f, 0.73f).setDuration(300);
                animatorContentBottom.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorContentBottom.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams) guidelineContentBottom.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    guidelineContentBottom.setLayoutParams(lp);

                });

                ValueAnimator animatorTitleEnd = ValueAnimator.ofFloat(0.75f, 0.975f);
                animatorTitleEnd.setDuration(300);
                animatorTitleEnd.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorTitleEnd.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams) guidelineTitleEnd.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    guidelineTitleEnd.setLayoutParams(lp);

                });

                animatorNotificationStart.start();
                animatorNotificationEnd.start();
                animatorTitleTop.start();
                animatorTitleBottom.start();
                animatorContentBottom.start();
                animatorTitleEnd.start();


                tileIcon.animate().translationX(0f).setDuration(400).start();

                notificationContent.setLines(1);
                notificationTitle.setGravity(Gravity.CENTER_VERTICAL);
                notificationContent.setGravity(Gravity.CENTER_VERTICAL);


                if (waveView.isEnabled())
                {
                    waveView.setEnabled(false);
                    factor.getUserApp().setMediaTile(false);
                    isMediaTile = false;
                    waveView.setAlpha(0);
                }
            }

            notificationState = newCount;
        }
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle()
    {
        return ViewKt.getLifecycle(this);
    }
}
