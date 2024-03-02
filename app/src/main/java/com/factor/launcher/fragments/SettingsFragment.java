package com.factor.launcher.fragments;

import static android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS;

import android.Manifest;
import android.app.role.RoleManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.factor.launcher.BuildConfig;
import com.factor.launcher.R;
import com.factor.launcher.databinding.FragmentSettingsBinding;
import com.factor.launcher.models.AppSettings;
import com.factor.launcher.models.IconPackProvider;
import com.factor.launcher.ui.CustomFlag;
import com.factor.launcher.ui.IconPackPickerView;
import com.factor.launcher.util.Constants;
import com.factor.launcher.util.Util;
import com.factor.launcher.view_models.AppSettingsManager;
import com.google.android.renderscript.Toolkit;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import eightbitlab.com.blurview.RenderScriptBlur;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

//todo: it's time to cleanup this page LOL
public class SettingsFragment extends Fragment implements LifecycleOwner
{
    private final String[] perms = {Manifest.permission.MANAGE_EXTERNAL_STORAGE};

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

    private FragmentSettingsBinding binding;

    private AppSettings settings;

    private String tileColor = "";

    private String searchColor = "";

    private Bitmap m;

    private String iconPack = "";

    private ActivityResultLauncher<Intent> resultLauncher;


    public SettingsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // DEAL WITH ERROR
            }
    );
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        updateSettings();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(getLayoutInflater());
        initializeComponents();
        return binding.getRoot();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        setUpUIState();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        updateSettings();
    }

    //initialize ui components
    private void initializeComponents()
    {
        if (getContext() == null || getActivity() == null)
            return;


        Drawable background = AppCompatResources.getDrawable(getContext(), R.drawable.city_welcome_background);

        assert background != null;

        m = Util.drawableToBitmap(background);

        int paddingHorizontal = (int) Util.dpToPx(20, getContext());

        AppSettingsManager appSettingsManager = AppSettingsManager.getInstance(getActivity().getApplication());

        settings = appSettingsManager.getAppSettings();

        binding.scrollView.setBindSpringToParent(true);

        tileColor = settings.getTileThemeColor();
        searchColor = settings.getSearchBarColor();

        binding.demoBlur.setupWith(binding.demoBackground, new RenderScriptBlur(getContext()))
                .setOverlayColor(Color.parseColor("#" + tileColor))
                .setBlurRadius(settings.getBlurRadius())
                .setBlurAutoUpdate(true);

        binding.searchBlur.setupWith(binding.demoBackground, new RenderScriptBlur(getContext()))
                .setOverlayColor(Color.parseColor("#" + searchColor))
                .setBlurRadius(25f)
                .setBlurAutoUpdate(true)
                .setBlurEnabled(true);

        binding.tileColorIcon.setBackgroundColor(Color.parseColor("#" + tileColor));
        binding.searchBarColorIcon.setBackgroundColor(Color.parseColor("#" + searchColor));

        binding.searchBase.setPadding(paddingHorizontal, 0, paddingHorizontal, 0);
        binding.searchBarColorValue.setText(searchColor);
        binding.tileColorValue.setText(tileColor);
        binding.searchBarColorValue.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                searchColor = s.toString();
                try
                {
                    binding.searchBarColorIcon.setBackgroundColor(Color.parseColor("#" + searchColor));
                    binding.searchBase.setCardBackgroundColor(Color.parseColor("#" + searchColor));
                    binding.searchBlur.setOverlayColor(Color.parseColor("#" + searchColor));
                }catch (Exception ignored){}
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.tileColorValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                tileColor = s.toString();
                try
                {
                    binding.tileColorIcon.setBackgroundColor(Color.parseColor("#" + tileColor));
                    binding.demoCard.setCardBackgroundColor(Color.parseColor("#" + tileColor));
                    binding.demoBlur.setOverlayColor(Color.parseColor("#" + tileColor));
                }
                catch (Exception ignored) {}

            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.searchBase.setCardBackgroundColor(Color.parseColor("#" + searchColor));

        binding.notificationAccessButton.setOnClickListener(v -> buildNotificationServiceAlertDialog());
        binding.storageAccessButton.setOnClickListener(v -> requestStoragePermission());
        binding.defaultLauncherButton.setOnClickListener(v ->
        {
            /*
            PackageManager p = getContext().getPackageManager();
            ComponentName cN = new ComponentName(getContext(), EmptyHome.class);
            p.setComponentEnabledSetting(cN, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            Intent selector = new Intent(Intent.ACTION_MAIN);
            selector.addCategory(Intent.CATEGORY_HOME);
            startActivity(selector);
            p.setComponentEnabledSetting(cN, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
             **/





            RoleManager roleManager = (RoleManager) requireActivity().getSystemService(Context.ROLE_SERVICE);
            if (roleManager.isRoleAvailable(RoleManager.ROLE_HOME))
            {
                Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME);

                resultLauncher.launch(intent);
            }






        });

        ((EditText)(binding.searchView.findViewById(R.id.search_src_text))).setTextColor(settings.isDarkIcon()?Color.BLACK:Color.WHITE);
        ((EditText)(binding.searchView.findViewById(R.id.search_src_text))).setHintTextColor(settings.isDarkIcon()?Color.DKGRAY:Color.LTGRAY);


        binding.menuButton.setImageResource(settings.isDarkIcon()? R.drawable.icon_menu_black : R.drawable.icon_menu);

        binding.demoCard.setCardBackgroundColor(Color.parseColor("#" + tileColor));


        binding.blurRadiusSlider.setValue(settings.getBlurRadius());
        binding.cornerRadiusSlider.setValue(settings.getCornerRadius());
        binding.tileListScaleSlider.setValue(settings.getTileListScale());
        binding.tileMarginSlider.setValue(settings.getTileMargin());


        binding.blurToggle.setChecked(settings.isBlurred());
        binding.blurToggle.setOnClickListener(v ->
        {
            setUpDemoTile();
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        });

        binding.staticBlurToggle.setChecked(settings.getStaticBlur());
        binding.staticBlurToggle.setOnClickListener(v ->
        {
            setUpDemoTile();
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        });

        binding.darkTextToggle.setChecked(settings.isDarkText());
        binding.darkTextToggle.setOnClickListener(v ->
        {
            setUpDemoTile();
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        });

        binding.iconShadowToggle.setChecked(settings.getShowShadowAroundIcon());
        binding.iconShadowToggle.setOnClickListener(v ->
        {
            setUpDemoTile();
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        });

        binding.darkIconToggle.setChecked(settings.isDarkIcon());
        binding.darkIconToggle.setOnClickListener(v ->
        {
            setUpDemoTile();
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        });


        binding.blurRadiusSlider.addOnChangeListener((slider, value, fromUser) ->
        {
            slider.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            setUpDemoTile();
        });

        binding.cornerRadiusSlider.addOnChangeListener((slider, value, fromUser) ->
        {
            slider.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            setUpDemoTile();
        });

        binding.tileListScaleSlider.addOnChangeListener((slider, value, fromUser) ->
        {
            slider.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            setUpDemoTile();
        });

        binding.tileMarginSlider.addOnChangeListener((slider, value, fromUser) ->
        {
            slider.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            setUpDemoTile();
        });

        binding.tileColorPickerButton.setOnClickListener(v -> showColorPickerDialog("Tile color"));
        binding.searchBarColorPickerButton.setOnClickListener(v -> showColorPickerDialog("Search bar color"));


        // Icon pack picker
        iconPack = settings.getIconPackPackageName();
        binding.iconPackPicker.setCurrentIconPack(iconPack);
        if (!iconPack.isEmpty())
        {
            binding.demoIconPack.setImageDrawable(binding.iconPackPicker.getCurrentIconPack().getIcon());
            binding.demoIconPack.setVisibility(View.VISIBLE);
            binding.tileIcon.setVisibility(View.INVISIBLE);
            binding.tileLabel.setText(binding.iconPackPicker.getCurrentIconPack().getLabelNew());
        }
        else
        {
            binding.tileLabel.setText(getContext().getString(R.string.no_icon_pack));
            binding.tileIcon.setVisibility(View.VISIBLE);
            binding.demoIconPack.setVisibility(View.INVISIBLE);
        }

        binding.iconPackPicker.setOnIconPackClickedListener(new IconPackPickerView.OnIconPackClickedListener()
        {
            @Override
            public void onIconPackClicked(IconPackProvider clickedIconPack)
            {
                assert getContext()!= null;
                if (clickedIconPack.isCurrentIconPack())
                {
                    binding.demoIconPack.setImageDrawable(clickedIconPack.getIcon());
                    binding.demoIconPack.setVisibility(View.VISIBLE);
                    binding.tileIcon.setVisibility(View.INVISIBLE);
                    iconPack = clickedIconPack.getPackageName();
                }
                else
                {
                    binding.demoIconPack.setVisibility(View.INVISIBLE);
                    binding.tileIcon.setVisibility(View.VISIBLE);
                    iconPack = "";
                }

                IconPackProvider i = binding.iconPackPicker.getCurrentIconPack();
                if (i.getPackageName().isEmpty() || !i.isCurrentIconPack())
                    binding.tileLabel.setText(getContext().getString(R.string.no_icon_pack));
                else
                    binding.tileLabel.setText(i.getLabelNew());
            }
        });

        setUpUIState();
        setUpDemoTile();
    }

    //update demo tile according to user settings
    private void setUpDemoTile()
    {
        if (getContext() == null || getActivity() == null)
            return;

        int padding = (int) Util.dpToPx(binding.tileMarginSlider.getValue(), getContext());
        binding.demoBase.setPadding(padding, padding, padding, padding);

        binding.rootCard.setRadius(Util.dpToPx(binding.cornerRadiusSlider.getValue(), getContext()));
        binding.demoCard.setRadius(Util.dpToPx(binding.cornerRadiusSlider.getValue(), getContext()));
        binding.togglesCard.setRadius(Util.dpToPx(binding.cornerRadiusSlider.getValue(), getContext()));
        binding.slidersCard.setRadius(Util.dpToPx(binding.cornerRadiusSlider.getValue(), getContext()));
        binding.demoBackground.setRadius(Util.dpToPx(binding.cornerRadiusSlider.getValue(), getContext()));
        binding.colorPickerCard.setRadius(Util.dpToPx(binding.cornerRadiusSlider.getValue(), getContext()));
        binding.advancedOptionsCard.setRadius(Util.dpToPx(binding.cornerRadiusSlider.getValue(), getContext()));
        binding.iconPackPicker.setRadius(Util.dpToPx(binding.cornerRadiusSlider.getValue(), getContext()));

        binding.demoBlur.setBlurRadius(binding.blurRadiusSlider.getValue());

        binding.menuButton.setImageResource(binding.darkIconToggle.isChecked()? R.drawable.icon_menu_black : R.drawable.icon_menu);
        binding.searchBase.setRadius(Util.dpToPx(binding.cornerRadiusSlider.getValue(), getContext()));
        ((EditText)(binding.searchView.findViewById(R.id.search_src_text))).setTextColor(binding.darkIconToggle.isChecked()?Color.BLACK:Color.WHITE);
        ((EditText)(binding.searchView.findViewById(R.id.search_src_text))).setHintTextColor(binding.darkIconToggle.isChecked()?Color.DKGRAY:Color.LTGRAY);


        binding.searchBlur.setBlurEnabled(binding.blurToggle.isChecked());

        if (binding.staticBlurToggle.isChecked())
        {
            binding.demoBlur.setVisibility(View.INVISIBLE);
        }
        else
            binding.demoBlur.setVisibility(binding.blurToggle.isChecked()?View.VISIBLE:View.INVISIBLE);

        binding.staticBlurPreviewImage.setVisibility(binding.blurToggle.isChecked()?View.VISIBLE:View.INVISIBLE);

        binding.blurRadiusSlider.setEnabled(binding.blurToggle.isChecked());
        binding.blurRadiusSlider.setAlpha(binding.blurToggle.isChecked()?1:0.5f);

        binding.staticBlurToggle.setEnabled(binding.blurToggle.isChecked());


        new Thread(() ->
        {


            Bitmap temp = m;
            Bitmap blurredM5;

            for (int i = 0; i < 10; i++)
            {
                temp = Toolkit.INSTANCE.blur(temp, (int) binding.blurRadiusSlider.getValue());
            }
            blurredM5 = temp;

            getActivity().runOnUiThread(() ->
            {
                binding.staticBlurPreviewImage.setImageBitmap(blurredM5);

                if (binding.staticBlurToggle.isChecked())
                    binding.staticBlurPreviewImage.setAlpha(1f);
                else
                    binding.staticBlurPreviewImage.setAlpha(0f);
            });

        }).start();


        binding.tileLabel.setTextColor(binding.darkTextToggle.isChecked()?Color.BLACK:Color.WHITE);
        binding.notificationTitle.setTextColor(binding.darkTextToggle.isChecked()?Color.BLACK:Color.WHITE);
        binding.notificationContent.setTextColor(binding.darkTextToggle.isChecked()?Color.BLACK:Color.WHITE);

        binding.tileIcon.setElevation(binding.iconShadowToggle.isChecked()? Util.dpToPx(50, getContext()):0);
    }

    //detect, save, and notify changes in app settings
    private void updateSettings()
    {
        if (areSettingsChanged() && getActivity() != null)
        {

            AppSettings updated = AppSettingsManager.getInstance(getActivity().getApplication()).getAppSettings();


            //save settings

            //toggles
            updated.setBlurred(binding.blurToggle.isChecked());
            updated.setDarkIcon(binding.darkIconToggle.isChecked());
            updated.setDarkText(binding.darkTextToggle.isChecked());
            updated.setShowShadowAroundIcon(binding.iconShadowToggle.isChecked());
            updated.setStaticBlur(binding.staticBlurToggle.isChecked());

            //sliders
            updated.setBlurRadius((int) binding.blurRadiusSlider.getValue());
            updated.setCornerRadius((int) binding.cornerRadiusSlider.getValue());
            updated.setTileListScale(binding.tileListScaleSlider.getValue());
            updated.setTileMargin((int) binding.tileMarginSlider.getValue());

            //colors
            updated.setTileThemeColor(tileColor);
            updated.setSearchBarColor(searchColor);

            updated.setIconPackPackageName(binding.iconPackPicker.getCurrentIconPackPackageName());


            AppSettingsManager.getInstance(getActivity().getApplication()).setAppSettings(updated).updateSettings();
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
                binding.tileListScaleSlider.getValue() != settings.getTileListScale() ||
                binding.tileMarginSlider.getValue() != settings.getTileMargin() ||
                !tileColor.equals(settings.getTileThemeColor()) ||
                !searchColor.equals(settings.getSearchBarColor()) ||
                binding.iconShadowToggle.isChecked() != settings.getShowShadowAroundIcon() ||
                binding.staticBlurToggle.isChecked() != settings.getStaticBlur() ||
                !iconPack.equals(settings.getIconPackPackageName());
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
        if (getContext() != null)
        {
            if (isNotificationServiceEnabled())
                startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
            else
            {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                alertDialogBuilder.setTitle("Factor Notification Service");
                alertDialogBuilder.setMessage("Please allow Factor Launcher to access your notifications");
                alertDialogBuilder.setPositiveButton("Ok", (dialog, id) -> startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS)));
                alertDialogBuilder.create().show();
            }
        }
    }

    //display color picker dialog
    private void showColorPickerDialog(String title)
    {
        ColorPickerDialog.Builder builder = new ColorPickerDialog.Builder(getContext())
                .setTitle(title)
                .setPositiveButton("Confirm",
                        (ColorEnvelopeListener) (envelope, fromUser) ->
                        {
                            if (title.equals("Tile color"))
                            {
                                tileColor = envelope.getHexCode();
                                binding.tileColorIcon.setBackgroundColor(Color.parseColor("#" + tileColor));
                                binding.demoCard.setCardBackgroundColor(Color.parseColor("#" + tileColor));
                                binding.demoBlur.setOverlayColor(Color.parseColor("#" + tileColor));

                                binding.tileColorValue.setText(tileColor);
                            }
                            else if (title.equals("Search bar color"))
                            {
                                searchColor = envelope.getHexCode();
                                binding.searchBarColorIcon.setBackgroundColor(Color.parseColor("#" + searchColor));
                                binding.searchBase.setCardBackgroundColor(Color.parseColor("#" + searchColor));
                                binding.searchBlur.setOverlayColor(Color.parseColor("#" + searchColor));

                                binding.searchBarColorValue.setText(searchColor);
                            }
                        })
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                .attachAlphaSlideBar(true)
                .attachBrightnessSlideBar(true)  // the default value is true.
                .setBottomSpace(12);

        builder.getColorPickerView().setFlagView(new CustomFlag(getContext(), R.layout.layout_color_picker_flag));
        if (title.equals("Tile color"))
            builder.getColorPickerView().setInitialColor(Color.parseColor("#" + tileColor));
        else if (title.equals("Search bar color"))
            builder.getColorPickerView().setInitialColor(Color.parseColor("#" + searchColor));
        builder.show();
    }

    private void requestStoragePermission()
    {
        EasyPermissions.requestPermissions
                (new PermissionRequest.Builder(this, Constants.STORAGE_PERMISSION_CODE, perms)
                        .setRationale(R.string.storage_permission_rationale)
                        .setPositiveButtonText(R.string.okay)
                        .setNegativeButtonText(R.string.cancel)
                        .setTheme(R.style.DialogTheme)
                        .build());

        startActivity(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + BuildConfig.APPLICATION_ID)));
    }

    //change UI enabled state and displayed text based on granted permissions
    private void setUpUIState()
    {
        binding.notificationAccessButton.setText(isNotificationServiceEnabled() ? getString(R.string.granted) : getString(R.string.request));

        if (getContext() != null)
            if (Environment.isExternalStorageManager()) {

                binding.storageAccessButton.setText(getString(R.string.granted));
                binding.blurToggleLabel.setText(R.string.blur_effect);
                binding.blurToggle.setClickable(true);
                binding.blurToggleBase.setOnClickListener(null);
            }
            else {

                binding.storageAccessButton.setText(getString(R.string.request));
                binding.blurToggleLabel.setText(R.string.blur_effect_missing_permission);
                binding.blurToggle.setChecked(false);
                binding.blurToggle.setClickable(false);
                binding.blurToggleBase.setOnClickListener(v -> requestStoragePermission());
            }
    }



}