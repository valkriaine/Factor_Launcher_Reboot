package com.factor.launcher.fragments;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.factor.launcher.R;
import com.factor.launcher.activities.EmptyHome;
import com.factor.launcher.databinding.FragmentSettingsBinding;
import com.factor.launcher.managers.AppSettingsManager;
import com.factor.launcher.models.AppSettings;
import com.factor.launcher.util.Constants;
import com.factor.launcher.util.Util;
import eightbitlab.com.blurview.RenderScriptBlur;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

import static android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS;


public class SettingsFragment extends Fragment
{
    private final String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE};

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
            binding.scrollView.setEdgeEffectColor(R.color.colorPrimary);
        }
        binding.demoBlur.setupWith(binding.demoBackground)
                .setOverlayColor(Color.parseColor(settings.getTransparentTileColor()))
                .setBlurAlgorithm(new RenderScriptBlur(getContext()))
                .setBlurRadius(settings.getBlurRadius())
                .setBlurAutoUpdate(true)
                .setHasFixedTransformationMatrix(true);

        binding.searchBlur.setupWith(binding.demoBackground)
                .setOverlayColor(Color.parseColor(settings.getTransparentSearchBarColor()))
                .setBlurAlgorithm(new RenderScriptBlur(getContext()))
                .setBlurRadius(25f)
                .setBlurAutoUpdate(true)
                .setHasFixedTransformationMatrix(false)
                .setBlurEnabled(true);

        binding.searchBase.setCardBackgroundColor(Color.parseColor(settings.getOpaqueSearchBarColor()));
        binding.searchBase.setRadius(Util.INSTANCE.dpToPx(settings.getCornerRadius(), getContext()));

        binding.notificationAccessButton.setText(isNotificationServiceEnabled() ? "Granted" : "Request");
        binding.notificationAccessButton.setOnClickListener(v -> buildNotificationServiceAlertDialog());
        binding.storageAccessButton.setOnClickListener(v ->
                EasyPermissions.requestPermissions(
                        new PermissionRequest.Builder(this, Constants.STORAGE_PERMISSION_CODE, perms)
                                .setRationale("Factor launcher needs to access your external storage")
                                .setPositiveButtonText("Okay")
                                .setNegativeButtonText("Cancel")
                                .setTheme(R.style.DialogTheme)
                                .build()));
        binding.defaultLauncherButton.setOnClickListener(v -> {
            PackageManager p = getContext().getPackageManager();
            ComponentName cN = new ComponentName(getContext(), EmptyHome.class);
            p.setComponentEnabledSetting(cN, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            Intent selector = new Intent(Intent.ACTION_MAIN);
            selector.addCategory(Intent.CATEGORY_HOME);
            startActivity(selector);
            p.setComponentEnabledSetting(cN, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        });

        ((EditText)(binding.searchView.findViewById(R.id.search_src_text))).setTextColor(settings.isDarkIcon()?Color.BLACK:Color.WHITE);
        ((EditText)(binding.searchView.findViewById(R.id.search_src_text))).setHintTextColor(settings.isDarkIcon()?Color.DKGRAY:Color.LTGRAY);


        binding.menuButton.setImageResource(settings.isDarkIcon()? R.drawable.icon_menu_black : R.drawable.icon_menu);

        binding.demoCard.setCardBackgroundColor(Color.parseColor(settings.getOpaqueTileColor()));
        binding.demoCard.setRadius(Util.INSTANCE.dpToPx(settings.getCornerRadius(), getContext()));

        binding.rootCard.setRadius(Util.INSTANCE.dpToPx(settings.getCornerRadius(), getContext()));
        binding.togglesCard.setRadius(Util.INSTANCE.dpToPx(settings.getCornerRadius(), getContext()));
        binding.slidersCard.setRadius(Util.INSTANCE.dpToPx(settings.getCornerRadius(), getContext()));
        binding.demoBackground.setRadius(Util.INSTANCE.dpToPx(settings.getCornerRadius(), getContext()));
        binding.colorPickerCard.setRadius(Util.INSTANCE.dpToPx(settings.getCornerRadius(), getContext()));
        binding.advancedOptionsCard.setRadius(Util.INSTANCE.dpToPx(settings.getCornerRadius(), getContext()));

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

        binding.rootCard.setRadius(Util.INSTANCE.dpToPx(binding.cornerRadiusSlider.getValue(), getContext()));
        binding.demoCard.setRadius(Util.INSTANCE.dpToPx(binding.cornerRadiusSlider.getValue(), getContext()));
        binding.togglesCard.setRadius(Util.INSTANCE.dpToPx(binding.cornerRadiusSlider.getValue(), getContext()));
        binding.slidersCard.setRadius(Util.INSTANCE.dpToPx(binding.cornerRadiusSlider.getValue(), getContext()));
        binding.demoBackground.setRadius(Util.INSTANCE.dpToPx(binding.cornerRadiusSlider.getValue(), getContext()));
        binding.colorPickerCard.setRadius(Util.INSTANCE.dpToPx(binding.cornerRadiusSlider.getValue(), getContext()));
        binding.advancedOptionsCard.setRadius(Util.INSTANCE.dpToPx(binding.cornerRadiusSlider.getValue(), getContext()));

        binding.demoBlur.setBlurRadius(binding.blurRadiusSlider.getValue());

        binding.menuButton.setImageResource(binding.darkIconToggle.isChecked()? R.drawable.icon_menu_black : R.drawable.icon_menu);
        binding.searchBase.setRadius(Util.INSTANCE.dpToPx(binding.cornerRadiusSlider.getValue(), getContext()));
        ((EditText)(binding.searchView.findViewById(R.id.search_src_text))).setTextColor(binding.darkIconToggle.isChecked()?Color.BLACK:Color.WHITE);
        ((EditText)(binding.searchView.findViewById(R.id.search_src_text))).setHintTextColor(binding.darkIconToggle.isChecked()?Color.DKGRAY:Color.LTGRAY);


        binding.searchBlur.setBlurEnabled(binding.blurToggle.isChecked());
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

    //check if settings are changed
    private boolean areSettingsChanged()
    {
        return binding.blurToggle.isChecked() != settings.isBlurred() ||
                binding.darkTextToggle.isChecked() != settings.isDarkText() ||
                binding.darkIconToggle.isChecked() != settings.isDarkIcon() ||
                binding.cornerRadiusSlider.getValue() != settings.getCornerRadius() ||
                binding.blurRadiusSlider.getValue() != settings.getBlurRadius() ||
                binding.iconShadowToggle.isChecked() != settings.getShowShadowAroundIcon();
    }


    //check if notification service is enabled
    private boolean isNotificationServiceEnabled()
    {
        if (getContext() == null)
            return true;
        String pkgName = getContext().getPackageName();
        final String flat = Settings.Secure.getString(getContext().getContentResolver(), ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat))
        {
            final String[] names = flat.split(":");
            for (String name : names)
            {
                final ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null && TextUtils.equals(pkgName, cn.getPackageName())) return true;
            }
        }
        return false;
    }

    //return dialog to request for notification access
    private void buildNotificationServiceAlertDialog()
    {
        if (getContext() == null)
            return;

        if (isNotificationServiceEnabled())
        {
            startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
            return;
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setTitle("Factor Notification Service");
        alertDialogBuilder.setMessage("Please allow Factor Launcher to access your notifications");
        alertDialogBuilder.setPositiveButton("Ok", (dialog, id) -> startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS)));
        alertDialogBuilder.create().show();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        binding.notificationAccessButton.setText(isNotificationServiceEnabled() ? "Granted" : "Request");

        if (getContext() == null)
            return;

        if (getContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            binding.storageAccessButton.setText("Granted");
        else
            binding.storageAccessButton.setText("Request");
    }
}