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


    //initialize ui components
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

        binding.demoCard.setCardBackgroundColor(Color.parseColor(settings.getOpaqueTileColor()));
        binding.demoCard.setRadius(Util.INSTANCE.dpToPx(settings.getCornerRadius(), getContext()));

        binding.blurToggle.setChecked(settings.isBlurred());
        binding.blurToggle.setOnClickListener(v -> setUpDemoTile());

        binding.darkTextToggle.setChecked(settings.isDarkText());
        binding.darkTextToggle.setOnClickListener(v -> setUpDemoTile());

        binding.iconShadowToggle.setChecked(settings.getShowShadowAroundIcon());
        binding.iconShadowToggle.setOnClickListener(v -> setUpDemoTile());

        binding.darkIconToggle.setChecked(settings.isDarkIcon());
        binding.darkIconToggle.setOnClickListener(v -> setUpDemoTile());

        binding.blurRadiusSlider.setValue(settings.getBlurRadius());
        binding.cornerRadiusSlider.setValue(settings.getCornerRadius());

        binding.blurRadiusSlider.addOnChangeListener((slider, value, fromUser) -> setUpDemoTile());
        binding.cornerRadiusSlider.addOnChangeListener((slider, value, fromUser) -> setUpDemoTile());


        //todo: add color pickers
        //todo: add advanced options

        setUpDemoTile();
    }

    //update demo tile according to user settings
    private void setUpDemoTile()
    {
        if (getContext() == null)
            return;

        binding.demoCard.setRadius(Util.INSTANCE.dpToPx(binding.cornerRadiusSlider.getValue(), getContext()));
        binding.demoBlur.setBlurRadius(binding.blurRadiusSlider.getValue());


        binding.demoBlur.setVisibility(binding.blurToggle.isChecked()?View.VISIBLE:View.INVISIBLE);
        binding.blurRadiusSlider.setEnabled(binding.blurToggle.isChecked());
        binding.blurRadiusSlider.setAlpha(binding.blurToggle.isChecked()?1:0.5f);


        binding.tileLabel.setTextColor(binding.darkTextToggle.isChecked()?Color.BLACK:Color.WHITE);
        binding.notificationTitle.setTextColor(binding.darkTextToggle.isChecked()?Color.BLACK:Color.WHITE);
        binding.notificationContent.setTextColor(binding.darkTextToggle.isChecked()?Color.BLACK:Color.WHITE);

        binding.tileIcon.setElevation(binding.iconShadowToggle.isChecked()?Util.INSTANCE.dpToPx(50, getContext()):0);
    }

    //detect, save, and notify changes in app settings
    private void updateSettings()
    {
        if (areSettingsChanged())
        {
            //save settings

            //toggles
            AppSettingsManager.getInstance(getContext()).getAppSettings().setBlurred(binding.blurToggle.isChecked());
            AppSettingsManager.getInstance(getContext()).getAppSettings().setDarkIcon(binding.darkIconToggle.isChecked());
            AppSettingsManager.getInstance(getContext()).getAppSettings().setDarkText(binding.darkTextToggle.isChecked());
            AppSettingsManager.getInstance(getContext()).getAppSettings().setShowShadowAroundIcon(binding.iconShadowToggle.isChecked());

            //sliders
            AppSettingsManager.getInstance(getContext()).getAppSettings().setBlurRadius((int) binding.blurRadiusSlider.getValue());
            AppSettingsManager.getInstance(getContext()).getAppSettings().setCornerRadius((int) binding.cornerRadiusSlider.getValue());

            //todo: save color pickers

            AppSettingsManager.getInstance(getContext()).updateSettings();
            Intent intent = new Intent();
            intent.setAction(Constants.SETTINGS_CHANGED);
            requireContext().sendBroadcast(intent);
        }
    }

    private boolean areSettingsChanged()
    {
        return binding.blurToggle.isChecked() != settings.isBlurred() ||
                binding.darkTextToggle.isChecked() != settings.isDarkText() ||
                binding.darkIconToggle.isChecked() != settings.isDarkIcon() ||
                binding.cornerRadiusSlider.getValue() != settings.getCornerRadius() ||
                binding.blurRadiusSlider.getValue() != settings.getBlurRadius() ||
                binding.iconShadowToggle.isChecked() != settings.getShowShadowAroundIcon();
    }
}