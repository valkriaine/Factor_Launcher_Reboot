package com.factor.launcher.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.factor.launcher.databinding.FragmentSettingsBinding;
import com.factor.launcher.managers.AppSettingsManager;
import com.factor.launcher.models.AppSettings;
import com.factor.launcher.util.Constants;
import com.factor.launcher.util.Util;
import eightbitlab.com.blurview.RenderScriptBlur;


public class SettingsFragment extends Fragment
{
    private FragmentSettingsBinding binding;

    private AppSettings settings;

    public SettingsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        updateSettings();
        binding = null;
        settings = null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(getLayoutInflater());
        initializeComponents();
        return binding.getRoot();
    }


    //todo: add more ui components
    private void initializeComponents()
    {
        if (getContext() == null)
            return;

        AppSettingsManager appSettingsManager = AppSettingsManager.getInstance(getContext());

        settings = appSettingsManager.getAppSettings();

        binding.demoBlur.setupWith(binding.demoBackground)
                .setOverlayColor(Color.parseColor(settings.getTransparentTileColor()))
                .setBlurAlgorithm(new RenderScriptBlur(getContext()))
                .setBlurRadius(settings.getBlurRadius())
                .setBlurAutoUpdate(true)
                .setHasFixedTransformationMatrix(true);


        binding.blurToggle.setChecked(settings.isBlurred());
        binding.blurToggle.setOnClickListener(v -> setUpDemoTile());

        setUpDemoTile();
    }

    //update demo tile according to user settings
    private void setUpDemoTile()
    {
        if (getContext() == null)
            return;

        binding.demoCard.setCardBackgroundColor(Color.parseColor(settings.getOpaqueTileColor()));
        binding.demoCard.setRadius(Util.INSTANCE.dpToPx(settings.getCornerRadius(), getContext()));

        if (binding.blurToggle.isChecked())
            binding.demoBlur.setVisibility(View.VISIBLE);
        else
            binding.demoBlur.setVisibility(View.INVISIBLE);
    }

    //detect, save, and notify changes in app settings
    private void updateSettings()
    {
        if (binding.blurToggle.isChecked() != settings.isBlurred())
        {
            AppSettingsManager.getInstance(getContext()).getAppSettings().setBlurred(binding.blurToggle.isChecked());
            AppSettingsManager.getInstance(getContext()).updateSettings();
            Intent intent = new Intent();
            intent.setAction(Constants.SETTINGS_CHANGED);
            requireContext().sendBroadcast(intent);
        }
    }
}