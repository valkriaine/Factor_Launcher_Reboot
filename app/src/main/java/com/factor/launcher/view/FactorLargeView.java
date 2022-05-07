package com.factor.launcher.view;

import android.animation.ValueAnimator;
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
import com.factor.launcher.util.Util;
import com.google.android.material.card.MaterialCardView;
import eightbitlab.com.blurview.*;

import java.util.List;

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

    private Guideline notificationDivider;

    private Guideline notificationDivider2;

    private Guideline notificationDivider3;

    private Guideline notificationDivider4;

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
        notificationDivider = findViewById(R.id.guideline_horizontal);
        notificationDivider2 = findViewById(R.id.guideline_horizontal2);
        notificationDivider3 = findViewById(R.id.guideline_horizontal3);
        notificationDivider4 = findViewById(R.id.guideline_horizontal4);
        notificationStart = findViewById(R.id.guideline_notification_content_start);
        notificationEnd = findViewById(R.id.guideline_notification_content_end);

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
                ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.25f, 0.055f);
                valueAnimator.setDuration(300);
                valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                valueAnimator.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams)notificationDivider.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    notificationDivider.setLayoutParams(lp);

                });

                ValueAnimator valueAnimator4 = ValueAnimator.ofFloat(0.25f, 0.135f);
                valueAnimator4.setDuration(300);
                valueAnimator4.setInterpolator(new AccelerateDecelerateInterpolator());
                valueAnimator4.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams)notificationDivider2.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    notificationDivider2.setLayoutParams(lp);

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

                ValueAnimator valueAnimator5 = ValueAnimator.ofFloat(0.355f, 0.48f);
                valueAnimator5.setDuration(300);
                valueAnimator5.setInterpolator(new AccelerateDecelerateInterpolator());
                valueAnimator5.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams)notificationDivider3.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    notificationDivider3.setLayoutParams(lp);

                });

                ValueAnimator valueAnimator6 = ValueAnimator.ofFloat(0.13f, 0.045f);
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

                notificationContent.setLines(4);
                notificationContent.setGravity(Gravity.NO_GRAVITY);
            }
            else if (newCount == 0)
            {
                // animate back to normal layout
                ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.15f, 0.25f);
                valueAnimator.setDuration(300);
                valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                valueAnimator.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams)notificationDivider.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    notificationDivider.setLayoutParams(lp);

                });

                ValueAnimator valueAnimator4 = ValueAnimator.ofFloat(0.135f, 0.25f);
                valueAnimator4.setDuration(300);
                valueAnimator4.setInterpolator(new AccelerateDecelerateInterpolator());
                valueAnimator4.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams)notificationDivider2.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    notificationDivider2.setLayoutParams(lp);

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

                ValueAnimator valueAnimator5 = ValueAnimator.ofFloat(0.48f, 0.355f);
                valueAnimator5.setDuration(300);
                valueAnimator5.setInterpolator(new AccelerateDecelerateInterpolator());
                valueAnimator5.addUpdateListener(valueAnimator1 ->
                {
                    LayoutParams lp = (LayoutParams)notificationDivider3.getLayoutParams();
                    lp.guidePercent = (float)valueAnimator1.getAnimatedValue();
                    notificationDivider3.setLayoutParams(lp);

                });

                ValueAnimator valueAnimator6 = ValueAnimator.ofFloat(0.045f, 0.13f);
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
                notificationContent.setGravity(Gravity.CENTER_VERTICAL);
            }

            notificationState = newCount;
        }
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
                catch (NullPointerException ignored){}
            }
        }
    }
}