package com.factor.launcher.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.*;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.ScriptIntrinsicBlur;
import androidx.viewpager.widget.ViewPager;
import com.factor.bouncy.util.OnOverPullListener;
import com.factor.chips.chipslayoutmanager.ChipsLayoutManager;
import com.factor.indicator_fast_scroll.FastScrollItemIndicator;
import com.factor.launcher.R;
import com.factor.launcher.activities.SettingsActivity;
import com.factor.launcher.databinding.FragmentHomeScreenBinding;
import com.factor.launcher.services.NotificationListener;
import com.factor.launcher.util.FirstCharUtil;
import com.factor.launcher.view_models.AppListManager;
import com.factor.launcher.view_models.AppSettingsManager;
import com.factor.launcher.models.AppSettings;
import com.factor.launcher.models.Factor;
import com.factor.launcher.models.UserApp;
import com.factor.launcher.receivers.AppActionReceiver;
import com.factor.launcher.receivers.NotificationBroadcastReceiver;
import com.factor.launcher.receivers.PackageActionsReceiver;
import com.factor.launcher.ui.FixedLinearLayoutManager;
import com.factor.launcher.util.Constants;
import com.factor.launcher.util.OnBackPressedCallBack;
import com.factor.launcher.util.Util;
import eightbitlab.com.blurview.RenderScriptBlur;
import java.util.ArrayList;


public class HomeScreenFragment extends Fragment implements OnBackPressedCallBack, LifecycleOwner
{
    private boolean isLiveWallpaper = false;

    private FragmentHomeScreenBinding binding;

    private WallpaperManager wm;

    private AppListManager appListManager;

    private AppActionReceiver appActionReceiver;

    private PackageActionsReceiver packageActionsReceiver;

    private NotificationBroadcastReceiver notificationBroadcastReceiver;

    private AppSettings appSettings;

    private RenderScriptBlur blurAlg;



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
                {
                    if (binding.appsList.getLayoutManager() != null)
                        (binding.appsList.getLayoutManager()).smoothScrollToPosition(binding.appsList, new RecyclerView.State(), 0);
                }
            }
            return true;
        }
        else if (binding.homePager.getCurrentItem() == 0)
        {
            if (getContext() == null)
                return true;

            if (binding.tilesList.getLayoutManager() != null)
            binding.tilesList.getLayoutManager().smoothScrollToPosition(binding.tilesList, new RecyclerView.State(), 0);
            return true;
        }
        else
            return true;
    }

    @Override
    public void onStop()
    {
        super.onStop();
        appListManager.saveRecentApps();
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        if (getContext() != null)
        {
            if (appActionReceiver != null)
            {
                appActionReceiver.invalidate();
                getContext().unregisterReceiver(appActionReceiver);
            }

            if (notificationBroadcastReceiver != null)
            {
                notificationBroadcastReceiver.invalidate();
                getContext().unregisterReceiver(notificationBroadcastReceiver);
            }

            if (packageActionsReceiver != null)
            {
                packageActionsReceiver.invalidate();
                getContext().unregisterReceiver(packageActionsReceiver);
            }
        }
    }


    @Override
    public void onResume()
    {
        super.onResume();
        if (getContext()!= null)
        {
            appListManager.clearAllNotifications();
            Intent intent = new Intent(getActivity(), NotificationListener.class);
            getContext().startService(intent);

            binding.recentAppsList.smoothScrollToPosition(0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
                appListManager.updateShortcuts();
        }
    }


    //initialize views and listeners
    @SuppressLint("ClickableViewAccessibility")
    private void initializeComponents()
    {

        if (getActivity() == null || getContext() == null)
            return;


        //initialize resources
        //***************************************************************************************************************************************************
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;


        int paddingTop105 = (int) Util.INSTANCE.dpToPx(105, getContext());
        int dp4 = (int) Util.INSTANCE.dpToPx(4, getContext());
        int dp20 = (int) Util.INSTANCE.dpToPx(20, getContext());

        int paddingTop;

        if (((float)height)/8 > paddingTop105)
            paddingTop = paddingTop105;
        else
            paddingTop = height/8;

        int paddingHorizontal = width / 20;
        int paddingBottom300 = (int) Util.INSTANCE.dpToPx(300, getContext());
        int paddingBottom150 = (int) Util.INSTANCE.dpToPx(150, getContext());
        int paddingBottomOnSearch = (int) Util.INSTANCE.dpToPx(2000, getContext());
        int appListPaddingTop100 = (int) Util.INSTANCE.dpToPx(100, getContext());



        blurAlg = new RenderScriptBlur(getContext());




        //initialize saved user settings
        appSettings = AppSettingsManager.getInstance(getActivity().getApplication()).getAppSettings();



        //tile list guideline position
        binding.guideline.setGuidelinePercent(appSettings.getTileListScale());

        //get system wallpaper
        //***************************************************************************************************************************************************
        checkLiveWallpaper();


        //initialize data manager
        //***************************************************************************************************************************************************
        appListManager = new AppListManager(this, binding.backgroundHost, isLiveWallpaper, appSettings);




        //register broadcast receivers
        //***************************************************************************************************************************************************
        registerBroadcastReceivers(appListManager, binding);


        //home pager
        //***************************************************************************************************************************************************
        binding.homePager.addView(binding.tilesPage, 0);
        binding.homePager.addView(binding.drawerPage, 1);




        //arrow button guideline
        if (paddingTop == paddingTop105)
            binding.guidelineArrowHorizontal.setGuidelinePercent((appListPaddingTop100 + dp20 - .5f) /height);
        else
            binding.guidelineArrowHorizontal.setGuidelinePercent((paddingTop + dp20 - .5f) /height);



        //app drawer
        //***************************************************************************************************************************************************
        if (paddingTop == paddingTop105)
            binding.appsList.setPadding(0, appListPaddingTop100, 0, paddingBottom150);
        else
            binding.appsList.setPadding(0, paddingTop + dp4, 0, paddingBottom150);


        Observer<ArrayList<UserApp>> appsObserver = userArrayList ->
        {
            binding.appsList.setLayoutManager(new FixedLinearLayoutManager(getContext()));
            binding.appsList.setAdapter(appListManager.adapter);
        };

        appListManager.getAppsMutableLiveData().observe(getViewLifecycleOwner(), appsObserver);

        binding.appsList.setHasFixedSize(true);
        binding.appsList.setItemViewCacheSize(appListManager.getListSize() * 2);




        LinearLayoutManager recentManager = new LinearLayoutManager(getContext());
        recentManager.setReverseLayout(true);
        binding.recentAppsList.setLayoutManager(recentManager);
        binding.recentAppsList.setAdapter(appListManager.recentAppsHost.getAdapter());


        //home pager on scroll
        binding.homePager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
                float xOffset = position + positionOffset;
                binding.arrowButton.setRotation(+180 * xOffset - 180);
                binding.searchBase.setTranslationY(-500f + 500 * xOffset);
                binding.searchView.clearFocus();

                if (paddingTop == paddingTop105)
                    binding.appsList.setPadding(0, appListPaddingTop100, 0, paddingBottom150);
                else
                    binding.appsList.setPadding(0, paddingTop + dp4, 0, paddingBottom150);
            }

            @Override
            public void onPageSelected(int position)
            {
                if (position == 0)
                {
                    binding.arrowButton.setRotation(180);
                    binding.blur.animate().alpha(0f);
                    binding.dim.animate().alpha(0f);
                }
                if (position == 1)
                {
                    binding.blur.animate().alpha(1f);
                    binding.dim.animate().alpha(1f);
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
                    char cap = '#';
                    if (item.getPackageName().isEmpty())
                        return new FastScrollItemIndicator.Text("");
                    if (item.getLabelNew().toUpperCase().length() != 0)
                        cap =  item.getLabelNew().toUpperCase().charAt(0);
                    String capString;
                    try
                    {
                        // if first letter is a number, return #
                        Integer.parseInt(String.valueOf(cap));
                        capString = "#";
                    }
                    catch (NumberFormatException ignored)
                    {
                        // not number
                        capString = FirstCharUtil.first(item.getLabelNew()).toUpperCase();
                    }
                    return new FastScrollItemIndicator.Text(capString);
                }
        );
        binding.thumb.setupWithFastScroller(binding.scrollBar);
        binding.scrollBar.setUseDefaultScroller(false);

        RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(getContext())
        {
            @Override protected int getVerticalSnapPreference()
            {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };

        binding.scrollBar.getItemIndicatorSelectedCallbacks().add(
                (indicator, indicatorCenterY, itemPosition) ->
                {
                    binding.appsList.stopScroll();
                    smoothScroller.setTargetPosition(itemPosition);
                    if (binding.appsList.getLayoutManager() != null)
                    binding.appsList.getLayoutManager().startSmoothScroll(smoothScroller);

                    // String selectedLetter = indicator.toString();

                    //todo: add touch event callback here
                    //key down while selectedLetter is not empty -> show list of apps
                    //key up clears selectedLetter, hide list of apps
                    //if key up on top of an app, launch the app
                }
        );


        //tile list
        //***************************************************************************************************************************************************
        binding.tilesList.setPadding(paddingHorizontal, paddingTop, width/5, paddingBottom300);


        Observer<ArrayList<Factor>> factorObserver = userArrayList ->
        {
            binding.tilesList
                    .setLayoutManager(ChipsLayoutManager.newBuilder(getContext())
                            .setOrientation(ChipsLayoutManager.HORIZONTAL)
                            .setChildGravity(Gravity.CENTER)
                            .setRowStrategy(ChipsLayoutManager.STRATEGY_DEFAULT)
                            .setMaxViewsInRow(8)
                            .setScrollingEnabled(true)
                            .build());

            binding.tilesList.setAdapter(appListManager.getFactorManager().adapter);
        };

        appListManager.getFactorManager().getFactorMutableLiveData().observe(getViewLifecycleOwner(), factorObserver);
        binding.tilesList.setItemViewCacheSize(20);
        binding.tilesList.setOrientation(1);




        binding.tilesList.setOnOverPullListener(new OnOverPullListener() {
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

        //select all text when the user clicks on the search bar
        //instead of clearing the search input
        ((EditText)(binding.searchView.findViewById(R.id.search_src_text))).setSelectAllOnFocus(true);

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
        binding.searchView.setOnQueryTextFocusChangeListener((v, hasFocus) ->
        {
            if (paddingTop == paddingTop105)
                binding.appsList.setPadding(0, appListPaddingTop100, 0, paddingBottom150);
            else
                binding.appsList.setPadding(0, paddingTop + dp4, 0, paddingBottom150);
        });




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


        //go to app drawer on click
        binding.arrowButton.setOnClickListener(view -> binding.homePager.setCurrentItem(1, true));

    }


    //setup wallpaper
    private void checkLiveWallpaper()
    {

        if (getContext() == null || getActivity() == null)
            return;

        binding.searchCard.setCardBackgroundColor(Color.parseColor("#" + appSettings.getSearchBarColor()));

        //static wallpaper
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                wm.getWallpaperInfo() == null && appSettings.isBlurred())
        {
            isLiveWallpaper = false;


            new Thread(() ->
            {
                Bitmap m = Util.INSTANCE.toBitmap(wm.getFastDrawable());

                RenderScript rs = RenderScript.create(getContext());

                for (int i = 0; i < 10; i++)
                {
                    final Allocation input = Allocation.createFromBitmap(rs, m);
                    final Allocation output = Allocation.createTyped(rs, input.getType());
                    final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

                    script.setRadius(25F);
                    script.setInput(input);
                    script.forEach(output);
                    output.copyTo(m);
                }

                getActivity().runOnUiThread(() -> binding.blur.setImageBitmap(m));
            }).start();


            binding.backgroundImage.setImageDrawable(wm.getDrawable());

            binding.searchBlur.setupWith(binding.rootContent)
                    .setOverlayColor(Color.parseColor("#" + appSettings.getSearchBarColor()))
                    .setFrameClearDrawable(wm.getDrawable())
                    .setBlurAlgorithm(blurAlg)
                    .setBlurRadius(25f)
                    .setBlurAutoUpdate(true)
                    .setHasFixedTransformationMatrix(false)
                    .setBlurEnabled(true);
        }
        else //live wallpaper
        {
            isLiveWallpaper = true;
            //binding.blur.setBlurEnabled(false);
            binding.searchBlur.setBlurEnabled(false);
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


        Intent intent = new Intent(getActivity(), NotificationListener.class);
        getContext().startService(intent);
    }

}