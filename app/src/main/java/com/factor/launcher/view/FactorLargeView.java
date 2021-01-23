package com.factor.launcher.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

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

            trans.setupWith(background)
                    .setOverlayColor(Color.parseColor("#" + appSettings.getTileThemeColor()))
                    .setBlurAlgorithm(new RenderScriptBlur(getContext()))
                    .setBlurRadius(appSettings.getBlurRadius())
                    .setBlurAutoUpdate(false)
                    .setHasFixedTransformationMatrix(false);
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
        tileIcon.setImageDrawable(factor.getIcon());



        ShortcutsAdapter adapter = new ShortcutsAdapter(factor.getUserApp().getShortCuts(), settings);

        shortcutAvailability.setVisibility(adapter.getItemCount() > 0 ? INVISIBLE : VISIBLE);

        shortcut_list.setAdapter(adapter);
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
                label.setText(shortcut.getLabel());
                icon.setImageDrawable(shortcut.getIcon());

                shortcutBase.setOnClickListener(shortcut.getLaunchEvent());
            }
        }
    }
}