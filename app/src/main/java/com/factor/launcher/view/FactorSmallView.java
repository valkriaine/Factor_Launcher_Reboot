package com.factor.launcher.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.factor.launcher.R;
import com.factor.launcher.models.AppSettings;
import com.factor.launcher.models.Factor;
import com.factor.launcher.ui.ElevationImageView;
import com.factor.launcher.util.Util;
import com.google.android.material.card.MaterialCardView;
import com.ssynhtn.waveview.WaveView;
import eightbitlab.com.blurview.BlurAlgorithm;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

import static androidx.core.app.NotificationCompat.CATEGORY_TRANSPORT;

/**
 * Small tile
 */
public class FactorSmallView extends ConstraintLayout
{
    private BlurView trans;

    private MaterialCardView card;

    private ElevationImageView tileIcon;

    private AppCompatButton notificationCount;

    private AppCompatTextView tileLabel;

    private WaveView waveView;

    private int notificationState = 0; //0 for no notification, 1 for otherwise

    private final WaveView.WaveData wave1 =
            new WaveView.WaveData((float)
                    (800 + Math.random() * 100),
                    (float) (100 + Math.random() * 20),
                    (float) (200 + Math.random() * 20),
                    (float) (Math.random() * 50),
                    Color.WHITE, Color.BLACK,
                    0.3f,
                    (long) (2000 + Math.random() * 1000),
                    true);

    private final WaveView.WaveData wave2 =
            new WaveView.WaveData((float)
                    (800 + Math.random() * 100),
                    (float) (100 + Math.random() * 20),
                    (float) (200 + Math.random() * 20),
                    (float) (Math.random() * 50),
                    Color.WHITE, Color.BLACK,
                    0.3f,
                    (long) (2000 + Math.random() * 1000),
                    false);

    private final WaveView.WaveData wave3 =
            new WaveView.WaveData((float)
                    (800 + Math.random() * 100),
                    (float) (100 + Math.random() * 20),
                    (float) (200 + Math.random() * 20),
                    (float) (Math.random() * 50),
                    Color.WHITE, Color.BLACK,
                    0.3f, (long)
                    (2000 + Math.random() * 1000),
                    false);

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
        if (isLiveWallpaper || !appSettings.isBlurred())
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

            trans.setupWith(background)
                    .setOverlayColor(Color.parseColor("#" + appSettings.getTileThemeColor()))
                    .setBlurAlgorithm(algorithm)
                    .setBlurRadius(appSettings.getBlurRadius())
                    .setBlurAutoUpdate(false);
        }

        card.setRadius(Util.INSTANCE.dpToPx(appSettings.getCornerRadius(), getContext()));
        tileIcon.setElevationDp(appSettings.getShowShadowAroundIcon()? 50 : 0);
    }

    public void setupContent(Factor factor)
    {
        tileLabel.setText(factor.getLabelNew());
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
                    && factor.getUserApp().getNotificationCategory() .equals(CATEGORY_TRANSPORT))
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
                    waveView.setAlpha(0);
                }

            }

            notificationState = newCount;
        }
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
    }


    // sound wave animation for media notification
    public void startWave()
    {
        waveView.setAlpha(1);
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
        if (waveView.isEnabled())
            waveView.startAnimation();
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        if (waveView.isEnabled())
            stopWave();
    }
}
