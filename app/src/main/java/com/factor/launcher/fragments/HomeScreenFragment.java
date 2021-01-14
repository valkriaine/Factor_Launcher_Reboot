package com.factor.launcher.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import com.factor.bouncy.util.OnOverPullListener;
import com.factor.chips.chipslayoutmanager.ChipsLayoutManager;
import com.factor.launcher.R;
import com.factor.launcher.activities.SettingsActivity;
import com.factor.launcher.databinding.FragmentHomeScreenBinding;
import com.factor.launcher.managers.AppListManager;
import com.factor.launcher.managers.AppSettingsManager;
import com.factor.launcher.models.AppSettings;
import com.factor.launcher.models.UserApp;
import com.factor.launcher.receivers.AppActionReceiver;
import com.factor.launcher.receivers.NotificationBroadcastReceiver;
import com.factor.launcher.receivers.PackageActionsReceiver;
import com.factor.launcher.util.Constants;
import com.factor.launcher.util.OnBackPressedCallBack;
import com.factor.launcher.util.Util;
import com.reddit.indicatorfastscroll.FastScrollItemIndicator;
import eightbitlab.com.blurview.RenderScriptBlur;

import java.util.Objects;


public class HomeScreenFragment extends Fragment implements OnBackPressedCallBack
{
    private boolean isLiveWallpaper = false;

    private FragmentHomeScreenBinding binding;

    private WallpaperManager wm;

    private AppListManager appListManager;

    private Context context;

    private AppActionReceiver appActionReceiver;

    private PackageActionsReceiver packageActionsReceiver;

    private NotificationBroadcastReceiver notificationBroadcastReceiver;

    private AppSettings appSettings;



    public HomeScreenFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        wm = WallpaperManager.getInstance(getContext());
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        binding = FragmentHomeScreenBinding.inflate(getLayoutInflater());
        binding.setLifecycleOwner(getViewLifecycleOwner());
        initializeComponents();
        return binding.getRoot();
    }

    //handle back button press
    @Override
    public boolean onBackPressed()
    {
        if (binding.homePager.getCurrentItem() == 1)
        {
            if (appListManager.isDisplayingHidden())
                binding.appsList.setAdapter(appListManager.setDisplayHidden(false));
            else
            {
                if (!binding.appsList.canScrollVertically(-1))
                    binding.homePager.setCurrentItem(0, true);
                else
                    Objects.requireNonNull(binding.appsList.getLayoutManager()).smoothScrollToPosition(binding.appsList, new RecyclerView.State(), 0);
            }
            return true;
        }
        else if (binding.homePager.getCurrentItem() == 0)
        {
            if (getContext() == null)
                return true;

            RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(getContext())
            {
                @Override protected int getVerticalSnapPreference()
                {
                    return LinearSmoothScroller.SNAP_TO_START;
                }
            };
            smoothScroller.setTargetPosition(0);
            Objects.requireNonNull((ChipsLayoutManager)binding.tilesList.getLayoutManager())
                    .smoothScrollToPosition(binding.tilesList, new RecyclerView.State(), 0);
            return true;
        }
        else
            return true;
    }


    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        if (context != null)
        {
            if (appActionReceiver != null)
            {
                appActionReceiver.invalidate();
                context.unregisterReceiver(appActionReceiver);
            }

            if (notificationBroadcastReceiver != null)
            {
                notificationBroadcastReceiver.invalidate();
                context.unregisterReceiver(notificationBroadcastReceiver);
            }

            if (packageActionsReceiver != null)
            {
                packageActionsReceiver.invalidate();
                context.unregisterReceiver(packageActionsReceiver);
            }

        }
        appListManager.invalidate();
        binding.homePager.setAdapter(null);
        binding.getRoot().clearFocus();
        binding.getRoot().clearAnimation();
        binding.searchBase.removeAllViews();
        context = null;
        binding = null;
        appListManager = null;
    }


    @Override
    public void onResume()
    {
        super.onResume();
        if (getContext()!= null)
        {
            Log.d("resumed", "resumed");
            appListManager.clearAllNotifications();
            Intent intent = new  Intent(Constants.NOTIFICATION_INTENT_ACTION_SETUP);
            getContext().sendBroadcast(intent);
        }
    }


    //initialize views and listeners
    @SuppressLint("ClickableViewAccessibility")
    private void initializeComponents()
    {
        //initialize resources
        //***************************************************************************************************************************************************

        if (getActivity() == null || getContext() == null)
            return;
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        int paddingTop = (int) Util.INSTANCE.dpToPx(105, getContext());
        int paddingHorizontal = (int) Util.INSTANCE.dpToPx(20, getContext());
        int paddingBottom300 = (int) Util.INSTANCE.dpToPx(300, getContext());
        int paddingBottom150 = (int) Util.INSTANCE.dpToPx(150, getContext());
        int paddingBottomOnSearch = (int) Util.INSTANCE.dpToPx(2000, getContext());
        int appListPaddingTop100 = (int) Util.INSTANCE.dpToPx(100, getContext());
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;


        //initialize saved user settings
        appSettings = AppSettingsManager.getInstance(getContext()).getAppSettings();


        //get system wallpaper
        //***************************************************************************************************************************************************
        checkLiveWallpaper();


        //initialize data manager
        //***************************************************************************************************************************************************
        appListManager = new AppListManager(this, binding.backgroundHost, isLiveWallpaper);



        //register broadcast receivers
        //***************************************************************************************************************************************************
        registerBroadcastReceivers(appListManager, binding);


        //home pager
        //***************************************************************************************************************************************************
        binding.homePager.addView(binding.tilesPage, 0);
        binding.homePager.addView(binding.drawerPage, 1);


        //app drawer
        //***************************************************************************************************************************************************
        binding.appsList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.appsList.setAdapter(appListManager.adapter);
        binding.appsList.setHasFixedSize(true);
        binding.appsList.setItemViewCacheSize(100);
        binding.homePager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
                float xOffset = position + positionOffset;
                binding.dim.setAlpha(xOffset);
                binding.arrowButton.setRotation(+180 * xOffset - 180);
                binding.blur.setAlpha(xOffset / 0.5f);
                binding.searchBase.setTranslationY(-500f + 500 * xOffset);
                binding.searchView.clearFocus();
                binding.appsList.setPadding(0, appListPaddingTop100, 0, paddingBottom150);
            }

            @Override
            public void onPageSelected(int position)
            {
                if (position == 0) {
                    binding.arrowButton.setRotation(180);
                    binding.blur.setAlpha(0f);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {}

        });
        binding.scrollBar.setupWithRecyclerView(
                binding.appsList,
                (position) ->
                {
                    UserApp item = appListManager.getUserApp(position);
                    if (item.getPackageName().isEmpty())
                        return new FastScrollItemIndicator.Text("");
                    char cap =  item.getLabelNew().toUpperCase().charAt(0);
                    String capString = item.getLabelNew().toUpperCase().substring(0, 1);
                    try
                    {
                        Integer.parseInt(String.valueOf(cap));
                        capString = "#";
                    }
                    catch (NumberFormatException ignored)
                    {
                        //todo: convert chinese to pinyin
                        //return new FastScrollItemIndicator.Icon("some drawable");
                    }

                    return new FastScrollItemIndicator.Text(capString);
                }
        );
        binding.thumb.setupWithFastScroller(binding.scrollBar);


        //tile list
        //***************************************************************************************************************************************************
        binding.tilesList.setPadding(paddingHorizontal, paddingTop, width / 5, paddingBottom300);
        binding.tilesList.setAdapter(appListManager.getFactorManager().adapter);
        binding.tilesList.setItemViewCacheSize(20);
        binding.tilesList
                .setLayoutManager(ChipsLayoutManager.newBuilder(getContext())
                .setOrientation(ChipsLayoutManager.HORIZONTAL)
                .setChildGravity(Gravity.CENTER)
                .setRowStrategy(ChipsLayoutManager.STRATEGY_DEFAULT)
                .setMaxViewsInRow(2)
                .setScrollingEnabled(true)
                .build());


        binding.tilesList.addOnOverPulledListener(new OnOverPullListener() {
            @Override
            public void onOverPulledTop(float v)
            {
                if (getContext()!= null && v >= 0.04)
                    Util.INSTANCE.setExpandNotificationDrawer(getContext(), true);
            }

            @Override
            public void onOverPulledBottom(float v) {}

            @Override
            public void onRelease() {}
        });


        //search bar
        //***************************************************************************************************************************************************
        binding.searchBase.setTranslationY(-500f);
        binding.searchCard.setRadius(Util.INSTANCE.dpToPx(appSettings.getCornerRadius(), getContext()));

        ((EditText)(binding.searchView.findViewById(R.id.search_src_text))).setTextColor(appSettings.isDarkIcon()?Color.BLACK:Color.WHITE);
        ((EditText)(binding.searchView.findViewById(R.id.search_src_text))).setHintTextColor(appSettings.isDarkIcon()?Color.DKGRAY:Color.LTGRAY);

        binding.searchView.setOnCloseListener(() ->
        {
            binding.appsList.setPadding(0, appListPaddingTop100, 0, paddingBottom150);
            return false;
        });
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                binding.appsList.setPadding(0, appListPaddingTop100, 0, paddingBottomOnSearch);
                String queryText = newText.toLowerCase().trim();
                appListManager.findPosition(binding.appsList, queryText);
                return true;
            }
        });
        binding.searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> binding.appsList.setPadding(0, appListPaddingTop100, 0, paddingBottom150));




        //menu button
        //***************************************************************************************************************************************************
        binding.menuButton.setImageResource(appSettings.isDarkIcon()? R.drawable.icon_menu_black : R.drawable.icon_menu);
        binding.menuButton.setOnClickListener(view ->
        {
            boolean isDisplayingHidden = appListManager.isDisplayingHidden();

            PopupMenu popup = new PopupMenu(getContext(), binding.menuButton);
            popup.getMenuInflater().inflate(R.menu.app_menu, popup.getMenu());

            MenuItem displayMode = popup.getMenu().getItem(0);
            MenuItem options = popup.getMenu().getItem(1);
            MenuItem wallpaperOption = popup.getMenu().getItem(2);

            //show hidden apps
            displayMode.setTitle(isDisplayingHidden ? "My apps" : "Hidden apps");
            displayMode.setOnMenuItemClickListener(item ->
            {
                binding.appsList.setAdapter(isDisplayingHidden?
                        appListManager.setDisplayHidden(false) : appListManager.setDisplayHidden(true));
                return true;
            });

            //launch settings
            options.setOnMenuItemClickListener(item ->
            {
                Intent intent = new Intent(getContext(), SettingsActivity.class);
                startActivity(intent);
                return true;
            });
            popup.show();

            //change system wallpaper
            wallpaperOption.setOnMenuItemClickListener(item ->
            {
                Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER);
                startActivity(Intent.createChooser(intent, getString(R.string.select_wallpaper)));
                return true;
            });
        });


        //test button to launch pick widget screen
        //todo: add this in a drop down menu or in the app drawer
        binding.addWidgetButton.setOnClickListener(view -> appListManager.launchPickWidgetIntent());

    }


    //setup wallpaper
    private void checkLiveWallpaper()
    {

        if (getContext() == null)
            return;

        binding.searchCard.setCardBackgroundColor(Color.parseColor("#" + appSettings.getSearchBarColor()));

        //static wallpaper
        if (getContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                wm.getWallpaperInfo() == null && appSettings.isBlurred())
        {
            isLiveWallpaper = false;

            binding.backgroundImage.setImageDrawable(wm.getDrawable());
            binding.blur.setupWith(binding.backgroundHost)
                    .setFrameClearDrawable(wm.getDrawable())
                    .setBlurAlgorithm(new RenderScriptBlur(getContext()))
                    .setBlurRadius(15f)
                    .setBlurAutoUpdate(false)
                    .setHasFixedTransformationMatrix(true)
                    .setBlurEnabled(true);

            binding.searchBlur.setupWith(binding.rootContent)
                    .setOverlayColor(Color.parseColor("#" + appSettings.getSearchBarColor()))
                    .setFrameClearDrawable(wm.getDrawable())
                    .setBlurAlgorithm(new RenderScriptBlur(getContext()))
                    .setBlurRadius(25f)
                    .setBlurAutoUpdate(true)
                    .setHasFixedTransformationMatrix(false)
                    .setBlurEnabled(true);
        }
        else //live wallpaper
        {
            isLiveWallpaper = true;
            binding.blur.setBlurEnabled(false);
            binding.searchBlur.setBlurEnabled(false);
            binding.blur.setBlurEnabled(false);
        }
    }


    //setup all broadcast receivers and notify NotificationListener when all receivers are ready
    private void registerBroadcastReceivers(AppListManager appListManager, FragmentHomeScreenBinding binding)
    {

        if (getContext() == null)
            return;

        appActionReceiver = new AppActionReceiver(appListManager, binding);
        IntentFilter filterAppAction = new IntentFilter();
        filterAppAction.addAction(Constants.BROADCAST_ACTION_REMOVE);
        filterAppAction.addAction(Constants.BROADCAST_ACTION_ADD);
        filterAppAction.addAction(Constants.BROADCAST_ACTION_RENAME);
        filterAppAction.addAction(Constants.SETTINGS_CHANGED);
        filterAppAction.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        getContext().registerReceiver(appActionReceiver, filterAppAction);

        packageActionsReceiver = new PackageActionsReceiver(appListManager);
        IntentFilter filterPackageAction = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filterPackageAction.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filterPackageAction.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filterPackageAction.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
        filterPackageAction.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
        filterPackageAction.addDataScheme("package");
        getContext().registerReceiver(packageActionsReceiver, filterPackageAction);


        notificationBroadcastReceiver = new NotificationBroadcastReceiver(appListManager);
        IntentFilter filterNotification = new IntentFilter();
        filterNotification.addAction(Constants.NOTIFICATION_INTENT_ACTION_CLEAR);
        filterNotification.addAction(Constants.NOTIFICATION_INTENT_ACTION_POST);
        getContext().registerReceiver(notificationBroadcastReceiver, filterNotification);


        Intent intent = new  Intent(Constants.NOTIFICATION_INTENT_ACTION_SETUP);
        getContext().sendBroadcast(intent);
    }
}