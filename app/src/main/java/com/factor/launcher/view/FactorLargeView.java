package com.factor.launcher.view;

import android.animation.ValueAnimator;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.factor.bouncy.BouncyRecyclerView;
import com.factor.launcher.R;
import com.factor.launcher.models.AppSettings;
import com.factor.launcher.models.AppShortcut;
import com.factor.launcher.models.Factor;
import com.factor.launcher.ui.ElevationImageView;
import com.factor.launcher.ui.WaveView;
import com.factor.launcher.util.Util;
import com.google.android.material.card.MaterialCardView;
import eightbitlab.com.blurview.*;

import java.util.List;

import static androidx.core.app.NotificationCompat.CATEGORY_TRANSPORT;

/**
 * Large tile
 */
public class FactorLargeView extends ConstraintLayout
{
    private BlurView trans;

    private MaterialCardView card;

    private ElevationImageView tileIcon;

    private AppCompatButton notificationCount;

    private AppCompatTextView tileLabel;

    private AppCompatTextView notificationTitle;

    private AppCompatTextView notificationContent;

    private AppCompatTextView shortcutAvailability;

    private LinearLayoutCompat divider;

    private BouncyRecyclerView shortcut_list;

    private AppSettings settings;

    private Guideline notificationStart;

    private Guideline notificationEnd;


    private Guideline guidelineTitleBottom;

    private Guideline guidelineContentBottom;

    private Guideline guidelineTitleTop;

    private Guideline guidelineTitleEnd;

    private WaveView waveView;

    private boolean isMediaTile = false;

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

    private int notificationState = 0; //0 for no notification, 1 for otherwise

    public FactorLargeView(Context context) {
        super(context);
        init();
    }

    public FactorLargeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FactorLargeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }


    private void init()
    {
        View.inflate(getContext(), R.layout.factor_large_view, this);
        trans = findViewById(R.id.trans);
        card = findViewById(R.id.card);
        tileLabel = findViewById(R.id.tileLabel);
        tileIcon = findViewById(R.id.tileIcon);
        notificationCount = findViewById(R.id.notification_count);
        notificationTitle = findViewById(R.id.notification_title);
        notificationContent = findViewById(R.id.notification_content);
        divider = findViewById(R.id.divider);
        shortcutAvailability = findViewById(R.id.shortcut_availability);
        shortcut_list = findViewById(R.id.shortcut_list);

        notificationStart = findViewById(R.id.guideline_notification_content_start);
        notificationEnd = findViewById(R.id.guideline_notification_content_end);
        guidelineTitleTop = findViewById(R.id.guideline_title_top);
        guidelineTitleBottom = findViewById(R.id.guideline_title_bottom);
        guidelineContentBottom = findViewById(R.id.guideline_content_bottom);
        guidelineTitleEnd = findViewById(R.id.guideline_notification_title_end);

        waveView = findViewById(R.id.wave);
        waveView.setEnabled(false);

        shortcut_list.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    private void setUpNotificationCount(String count)
    {
        notificationCount.setVisibility(count.equals("0") ? INVISIBLE : VISIBLE);
        notificationCount.setText(count);
    }

    public void setupTile(AppSettings appSettings, boolean isLiveWallpaper, ViewGroup background)
    {
        //label color
        this.settings = appSettings;

        tileLabel.setTextColor(appSettings.isDarkText() ? Color.BLACK : Color.WHITE);
        notificationTitle.setTextColor(appSettings.isDarkText() ? Color.BLACK : Color.WHITE);
        notificationContent.setTextColor(appSettings.isDarkText() ? Color.BLACK : Color.WHITE);
        shortcutAvailability.setTextColor(appSettings.isDarkText() ? Color.BLACK : Color.WHITE);

        divider.setBackgroundColor(appSettings.isDarkText() ? Color.BLACK : Color.WHITE);

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
        isMediaTile = factor.isMediaTile();
        notificationTitle.setText(factor.getUserApp().getNotificationTitle());
        notificationContent.setText(factor.getUserApp().getNotificationText());

        tileLabel.setText(factor.getLabelNew());

        if (factor.getIcon() != null)
            tileIcon.setImageDrawable(factor.getIcon());


        updateLayout(factor);

        ShortcutsAdapter adapter = new ShortcutsAdapter(factor.getUserApp().getShortCuts(), settings);

        shortcutAvailability.setVisibility(adapter.getItemCount() > 0 ? INVISIBLE : VISIBLE);

        shortcut_list.setAdapter(adapter);
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

                ValueAnimator animatorTitleTop = ValueAnimator.ofFloat(0.13f, 0.025f);
                animatorTitleTop.setDuration(300);
                animatorTitleTop.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorTitleTop.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams) guidelineTitleTop.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    guidelineTitleTop.setLayoutParams(lp);

                });

                ValueAnimator animatorTitleBottom = ValueAnimator.ofFloat(0.25f, 0.135f);
                animatorTitleBottom.setDuration(300);
                animatorTitleBottom.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorTitleBottom.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams) guidelineTitleBottom.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    guidelineTitleBottom.setLayoutParams(lp);

                });

                ValueAnimator animatorContentBottom = ValueAnimator.ofFloat(0.355f, 0.48f);
                animatorContentBottom.setDuration(300);
                animatorContentBottom.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorContentBottom.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams) guidelineContentBottom.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    guidelineContentBottom.setLayoutParams(lp);

                });


                ValueAnimator animatorTitleEnd = ValueAnimator.ofFloat(0.975f, 0.8f);
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

                notificationContent.setLines(4);
                notificationTitle.setGravity(Gravity.NO_GRAVITY);
                notificationContent.setGravity(Gravity.NO_GRAVITY);


                if (factor.getUserApp().getNotificationCategory() != null
                        && factor.getUserApp().getNotificationCategory() .equals(CATEGORY_TRANSPORT) && factor.isMediaTile())
                {
                    setupWaves(factor);
                    startWave();
                }

            }
            else if (newCount == 0)
            {
                // animate back to normal layout
                ValueAnimator animatorNotificationStart = ValueAnimator.ofFloat(0.05f, 0.4f);
                animatorNotificationStart.setDuration(300);
                animatorNotificationStart.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorNotificationStart.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams)notificationStart.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    notificationStart.setLayoutParams(lp);

                });

                ValueAnimator animatorNotificationEnd = ValueAnimator.ofFloat(0.95f, 0.97f);
                animatorNotificationEnd.setDuration(300);
                animatorNotificationEnd.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorNotificationEnd.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams)notificationEnd.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    notificationEnd.setLayoutParams(lp);

                });

                ValueAnimator animatorTitleTop = ValueAnimator.ofFloat(0.025f, 0.13f);
                animatorTitleTop.setDuration(300);
                animatorTitleTop.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorTitleTop.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams) guidelineTitleTop.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    guidelineTitleTop.setLayoutParams(lp);

                });

                ValueAnimator animatorTitleBottom = ValueAnimator.ofFloat(0.135f, 0.25f);
                animatorTitleBottom.setDuration(300);
                animatorTitleBottom.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorTitleBottom.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams) guidelineTitleBottom.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    guidelineTitleBottom.setLayoutParams(lp);

                });

                ValueAnimator animatorContentBottom = ValueAnimator.ofFloat(0.48f, 0.355f);
                animatorContentBottom.setDuration(300);
                animatorContentBottom.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorContentBottom.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams) guidelineContentBottom.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    guidelineContentBottom.setLayoutParams(lp);

                });


                ValueAnimator animatorTitleEnd = ValueAnimator.ofFloat(0.8f, 0.975f);
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
                    factor.setMediaTile(false);
                    isMediaTile = false;
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








    protected static class ShortcutsAdapter extends RecyclerView.Adapter<ShortcutsAdapter.ShortcutViewHolder>
    {

        private final List<AppShortcut> shortcuts;

        private final AppSettings settings;

        public ShortcutsAdapter(List<AppShortcut> shortcuts, AppSettings appSettings)
        {
            this.shortcuts = shortcuts;
            this.settings = appSettings;
        }


        @NonNull
        @Override
        public ShortcutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shortcut_item, parent, false);

            //resize to fit screen
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            int scale = parent.getWidth();
            layoutParams.width = scale/3;
            view.setLayoutParams(layoutParams);

            return new ShortcutViewHolder(view, settings, layoutParams.width);
        }

        @Override
        public void onBindViewHolder(@NonNull ShortcutViewHolder holder, int position)
        {
            holder.bind(shortcuts.get(position));
        }

        @Override
        public int getItemCount()
        {
            return shortcuts.size();
        }

        protected static class ShortcutViewHolder extends RecyclerView.ViewHolder
        {

            private final AppCompatTextView label;

            private final AppCompatImageView icon;

            private final ConstraintLayout shortcutBase;

            public ShortcutViewHolder(@NonNull View itemView, AppSettings settings, int width)
            {
                super(itemView);

                CardView card = itemView.findViewById(R.id.shortcut_card);
                label = itemView.findViewById(R.id.shortcut_label);
                icon = itemView.findViewById(R.id.shortcut_icon);
                shortcutBase = itemView.findViewById(R.id.shortcut_base);

                card.setElevation(settings.getShowShadowAroundIcon()? Util.INSTANCE.dpToPx(10, itemView.getContext()) : 0);

                ViewGroup.LayoutParams params = card.getLayoutParams();

                params.width = (int) (width * .4);
                params.height = (int) (width * .4);
                card.setLayoutParams(params);

                card.setRadius((params.width + .5f)/2);

                label.setTextColor(settings.isDarkText() ? Color.BLACK : Color.WHITE);

            }

            public void bind(AppShortcut shortcut)
            {
                try
                {
                    label.setText(shortcut.getLabel());
                    icon.setImageDrawable(shortcut.getIcon());
                    shortcutBase.setOnClickListener(shortcut.getLaunchEvent());
                }
                catch (NullPointerException | ActivityNotFoundException ignored){}
            }
        }
    }
}