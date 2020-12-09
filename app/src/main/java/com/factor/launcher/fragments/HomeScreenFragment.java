package com.factor.launcher.fragments;

import android.app.WallpaperManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager.widget.ViewPager;
import com.factor.launcher.R;
import com.factor.launcher.databinding.FragmentHomeScreenBinding;
import com.factor.launcher.managers.AppListManager;
import com.factor.launcher.receivers.PackageActionsReceiver;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;


public class HomeScreenFragment extends Fragment
{
    private FragmentHomeScreenBinding binding;

    private WallpaperManager wm;

    private final long timeUnit = (long) 0.000001;


    public HomeScreenFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        wm = WallpaperManager.getInstance(requireContext());
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home_screen, container, false);
        initializeComponents();
        return binding.getRoot();
    }


    private void initializeComponents()
    {
        binding.image.setImageDrawable(wm.getDrawable());
        AppListManager appListManager = new AppListManager(this.requireActivity(), binding.backgroundHost);
        PackageActionsReceiver packageActionsReceiver = new PackageActionsReceiver(appListManager);

        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");

        requireActivity().registerReceiver(packageActionsReceiver, filter);

        //home pager
        binding.homePager.addView(binding.tilesPage, 0);
        binding.homePager.addView(binding.drawerPage, 1);

        //app drawer
        binding.appsList.setLayoutManager(new LinearLayoutManager(this.getContext()));
        binding.appsList.setAdapter(appListManager.adapter);

        //tile list
        FlexboxLayoutManager flex = new FlexboxLayoutManager(this.getContext());
        flex.setFlexDirection(FlexDirection.ROW);
        flex.setJustifyContent(JustifyContent.FLEX_START);
        binding.tilesList.setLayoutManager(flex);
        binding.tilesList.setAdapter(appListManager.getFactorManager().adapter);

        binding.tilesList.getRecycledViewPool().setMaxRecycledViews(0, 0);
        binding.blur.setViewBehind(binding.backgroundHost);


        binding.homePager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
                float xOffset = position + positionOffset;
                binding.dim.setAlpha(xOffset / 0.5f);
                binding.arrowButton.setRotation(+180 * xOffset - 180);

                binding.blur.enable();
                binding.blur.setAlpha(xOffset / 0.5f);
                binding.blur.setBlurRadius(xOffset * 40);
                binding.blur.updateForMilliSeconds(timeUnit);
            }

            @Override
            public void onPageSelected(int position)
            {
                if (position == 0)
                {
                    binding.arrowButton.setRotation(180);
                    binding.blur.setAlpha(0);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {
            }
        });
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        binding.blur.disable();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        binding.blur.disable();
    }

}