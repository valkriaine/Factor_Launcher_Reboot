package com.factor.launcher.fragments;

import android.app.WallpaperManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager.widget.ViewPager;
import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager;
import com.factor.launcher.R;
import com.factor.launcher.databinding.FragmentHomeScreenBinding;
import com.factor.launcher.managers.AppListManager;
import com.factor.launcher.receivers.PackageActionsReceiver;
import eightbitlab.com.blurview.RenderScriptBlur;


public class HomeScreenFragment extends Fragment
{
    private FragmentHomeScreenBinding binding;

    private WallpaperManager wm;


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
        ChipsLayoutManager chips = ChipsLayoutManager.newBuilder(requireContext())
                .setOrientation(ChipsLayoutManager.HORIZONTAL)
                .setChildGravity(Gravity.CENTER)
                .setRowStrategy(ChipsLayoutManager.STRATEGY_DEFAULT)
                .setMaxViewsInRow(2)
                .setScrollingEnabled(true)
                .build();

        //todo: research on grid layout manager changing both column and row span
        //StaggeredGridLayoutManager grid = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

        binding.tilesList.setLayoutManager(chips);
        binding.tilesList.setAdapter(appListManager.getFactorManager().adapter);



        binding.blur.setupWith(binding.backgroundHost)
                .setFrameClearDrawable(wm.getDrawable())
                .setBlurAlgorithm(new RenderScriptBlur(requireContext()))
                .setBlurRadius(10f)
                .setBlurAutoUpdate(false)
                .setHasFixedTransformationMatrix(true);



        binding.homePager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
                float xOffset = position + positionOffset;
                binding.dim.setAlpha(xOffset / 0.5f);
                binding.arrowButton.setRotation(+180 * xOffset - 180);

                binding.blur.setAlpha(xOffset / 0.5f);
            }

            @Override
            public void onPageSelected(int position)
            {
                if (position == 0)
                {
                    binding.arrowButton.setRotation(180);
                    binding.blur.setAlpha(0f);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {
            }
        });
    }
}