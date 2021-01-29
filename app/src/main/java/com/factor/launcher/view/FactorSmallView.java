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
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

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
    }

    private void setUpNotificationCount(String count)
    {
        notificationCount.setVisibility(count.equals("0") ? INVISIBLE : VISIBLE);
        notificationCount.setText(count);
    }

    public void setupTile(AppSettings appSettings, boolean isLiveWallpaper, ViewGroup background, RenderScriptBlur blurAlg)
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

            trans.setupWith(background)
                    .setOverlayColor(Color.parseColor("#" + appSettings.getTileThemeColor()))
                    .setBlurAlgorithm(blurAlg)
                    .setBlurRadius(appSettings.getBlurRadius())
                    .setBlurAutoUpdate(false)
                    .setHasFixedTransformationMatrix(false);
        }

        card.setRadius(Util.INSTANCE.dpToPx(appSettings.getCornerRadius(), getContext()));
        tileIcon.setElevationDp(appSettings.getShowShadowAroundIcon()? 50 : 0);
    }

    public void setupContent(Factor factor)
    {
        tileLabel.setText(factor.getLabelNew());
        setUpNotificationCount(factor.retrieveNotificationCount());
        tileIcon.setImageDrawable(factor.getIcon());
    }
}
