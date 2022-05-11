package com.factor.launcher.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import eightbitlab.com.blurview.BlurAlgorithm;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

import static androidx.core.app.NotificationCompat.CATEGORY_TRANSPORT;

/**
 * Small tile
 */
public class FactorSmallView extends ConstraintLayout implements LifecycleOwner
{
    private boolean isMediaTile = false;
    private int notificationState = 0; //0 for no notification
    private BlurView trans;

    private MaterialCardView card;

    private ElevationImageView tileIcon;

    private AppCompatButton notificationCount;

    private AppCompatTextView tileLabel;

    private WaveView waveView;


    private final WaveView.WaveData wave1 = Waves.generateWave();

    private final WaveView.WaveData wave2 = Waves.generateWave();

    private final WaveView.WaveData wave3 = Waves.generateWave();
    public FactorSmallView(Context context) {
        super(context);
        init();
    }

    public FactorSmallView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FactorSmallView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }


    private void init()
    {
        View.inflate(getContext(), R.layout.factor_small_view, this);
        trans = findViewById(R.id.trans);
        card = findViewById(R.id.card);
        tileLabel = findViewById(R.id.tileLabel);
        tileIcon = findViewById(R.id.tileIcon);
        notificationCount = findViewById(R.id.notification_count);

        waveView = findViewById(R.id.wave);
        waveView.setEnabled(false);
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

        //initialize blur and color
        if (isLiveWallpaper || !appSettings.isBlurred() || appSettings.getStaticBlur())
        {
            trans.setVisibility(INVISIBLE);
            card.setCardBackgroundColor(Color.parseColor("#" + appSettings.getTileThemeColor()));
        }
        else
        {
            //blur enabled, non static blur
            trans.setVisibility(VISIBLE);
            card.setCardBackgroundColor(Color.TRANSPARENT);

            BlurAlgorithm algorithm;

            algorithm = new RenderScriptBlur(getContext());

            trans.setupWith(background)
                    .setOverlayColor(Color.parseColor("#" + appSettings.getTileThemeColor()))
                    .setBlurAlgorithm(algorithm)
                    .setBlurRadius(appSettings.getBlurRadius())
                    .setBlurAutoUpdate(false);
        }

        card.setRadius(Util.dpToPx(appSettings.getCornerRadius(), getContext()));
        tileIcon.setElevationDp(appSettings.getShowShadowAroundIcon()? 50 : 0);
    }


    public void setupContent(Factor factor)
    {
        tileLabel.setText(factor.getLabelNew());
        isMediaTile = factor.getUserApp().isMediaTile();
        setUpNotificationCount(factor.retrieveNotificationCount());
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


    private void setupWaves(Factor factor)
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
        isMediaTile = factor.getUserApp().isMediaTile();
    }


    // sound wave animation for media notification
    private void startWave()
    {
        if (waveView.getAlpha() != 1)
            waveView.animate().alpha(1).setDuration(150).start();

        waveView.startAnimation();

    }

    private void stopWave()
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

    @NonNull
    @Override
    public Lifecycle getLifecycle()
    {
        return ViewKt.getLifecycle(this);
    }
}
