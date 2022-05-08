package com.factor.launcher.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import com.factor.launcher.R;
import com.factor.launcher.models.AppSettings;
import com.factor.launcher.models.Factor;
import com.factor.launcher.ui.ElevationImageView;
import com.factor.launcher.util.Util;
import com.google.android.material.card.MaterialCardView;
import eightbitlab.com.blurview.*;

/**
 * Medium tile
 */
public class FactorMediumView extends ConstraintLayout
{
    private BlurView trans;

    private MaterialCardView card;

    private ElevationImageView tileIcon;

    private AppCompatButton notificationCount;

    private AppCompatTextView tileLabel;

    private AppCompatTextView notificationTitle;

    private AppCompatTextView notificationContent;

    private Guideline notificationStart;

    private Guideline notificationEnd;

    private Guideline notificationDivider;

    private Guideline notificationDivider2;

    private Guideline notificationDivider3;

    private Guideline notificationDivider4;

    private int notificationState = 0; //0 for no notification, 1 for otherwise

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
        notificationDivider = findViewById(R.id.guideline_horizontal);
        notificationDivider2 = findViewById(R.id.guideline_horizontal2);
        notificationDivider3 = findViewById(R.id.guideline_horizontal3);
        notificationDivider4 = findViewById(R.id.guideline_horizontal4);
        notificationStart = findViewById(R.id.guideline_notification_content_start);
        notificationEnd = findViewById(R.id.guideline_notification_content_end);
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

        setUpNotificationCount(factor.retrieveNotificationCount());

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
                ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.5f, 0.11f);
                valueAnimator.setDuration(300);
                valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                valueAnimator.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams)notificationDivider.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    notificationDivider.setLayoutParams(lp);

                });

                ValueAnimator valueAnimator2 = ValueAnimator.ofFloat(0.4f, 0.05f);
                valueAnimator2.setDuration(300);
                valueAnimator2.setInterpolator(new AccelerateDecelerateInterpolator());
                valueAnimator2.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams)notificationStart.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    notificationStart.setLayoutParams(lp);

                });

                ValueAnimator valueAnimator3 = ValueAnimator.ofFloat(0.97f, 0.95f);
                valueAnimator3.setDuration(300);
                valueAnimator3.setInterpolator(new AccelerateDecelerateInterpolator());
                valueAnimator3.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams)notificationEnd.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    notificationEnd.setLayoutParams(lp);

                });

                ValueAnimator valueAnimator4 = ValueAnimator.ofFloat(0.5f, 0.27f);
                valueAnimator4.setDuration(300);
                valueAnimator4.setInterpolator(new AccelerateDecelerateInterpolator());
                valueAnimator4.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams)notificationDivider2.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    notificationDivider2.setLayoutParams(lp);

                });

                ValueAnimator valueAnimator5 = ValueAnimator.ofFloat(0.73f, 0.85f);
                valueAnimator5.setDuration(300);
                valueAnimator5.setInterpolator(new AccelerateDecelerateInterpolator());
                valueAnimator5.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams)notificationDivider3.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    notificationDivider3.setLayoutParams(lp);

                });

                ValueAnimator valueAnimator6 = ValueAnimator.ofFloat(0.27f, 0.05f);
                valueAnimator6.setDuration(300);
                valueAnimator6.setInterpolator(new AccelerateDecelerateInterpolator());
                valueAnimator6.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams)notificationDivider4.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    notificationDivider4.setLayoutParams(lp);

                });

                valueAnimator.start();
                valueAnimator2.start();
                valueAnimator3.start();
                valueAnimator4.start();
                valueAnimator5.start();
                valueAnimator6.start();

                tileIcon.animate().translationX(-500f).setDuration(400).start();

                notificationContent.setLines(3);
                notificationTitle.setGravity(Gravity.NO_GRAVITY);
                notificationContent.setGravity(Gravity.NO_GRAVITY);

            }
            else if (newCount == 0)
            {
                // animate back to normal layout
                ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.11f, 0.5f);
                valueAnimator.setDuration(300);
                valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                valueAnimator.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams)notificationDivider.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    notificationDivider.setLayoutParams(lp);

                });


                ValueAnimator valueAnimator2 = ValueAnimator.ofFloat(0.05f, 0.4f);
                valueAnimator2.setDuration(300);
                valueAnimator2.setInterpolator(new AccelerateDecelerateInterpolator());
                valueAnimator2.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams)notificationStart.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    notificationStart.setLayoutParams(lp);

                });

                ValueAnimator valueAnimator3 = ValueAnimator.ofFloat(0.95f, 0.97f);
                valueAnimator3.setDuration(300);
                valueAnimator3.setInterpolator(new AccelerateDecelerateInterpolator());
                valueAnimator3.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams)notificationEnd.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    notificationEnd.setLayoutParams(lp);

                });

                ValueAnimator valueAnimator4 = ValueAnimator.ofFloat(0.27f, 0.5f);
                valueAnimator4.setDuration(300);
                valueAnimator4.setInterpolator(new AccelerateDecelerateInterpolator());
                valueAnimator4.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams)notificationDivider2.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    notificationDivider2.setLayoutParams(lp);

                });

                ValueAnimator valueAnimator5 = ValueAnimator.ofFloat(0.85f, 0.73f);
                valueAnimator5.setDuration(300);
                valueAnimator5.setInterpolator(new AccelerateDecelerateInterpolator());
                valueAnimator5.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams)notificationDivider3.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    notificationDivider3.setLayoutParams(lp);

                });

                ValueAnimator valueAnimator6 = ValueAnimator.ofFloat(0.05f, 0.27f);
                valueAnimator6.setDuration(300);
                valueAnimator6.setInterpolator(new AccelerateDecelerateInterpolator());
                valueAnimator6.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams)notificationDivider4.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    notificationDivider4.setLayoutParams(lp);

                });

                valueAnimator.start();
                valueAnimator2.start();
                valueAnimator3.start();
                valueAnimator4.start();
                valueAnimator5.start();
                valueAnimator6.start();

                tileIcon.animate().translationX(0f).setDuration(400).start();

                notificationContent.setLines(1);
                notificationTitle.setGravity(Gravity.CENTER_VERTICAL);
                notificationContent.setGravity(Gravity.CENTER_VERTICAL);
            }

            notificationState = newCount;
        }
    }
}
