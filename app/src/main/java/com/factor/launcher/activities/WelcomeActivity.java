package com.factor.launcher.activities;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager.widget.ViewPager;
import com.factor.launcher.R;
import com.factor.launcher.databinding.ActivityWelcomeBinding;
import com.factor.launcher.util.Constants;
import eightbitlab.com.blurview.RenderScriptBlur;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

import java.util.List;

import static android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS;
import static com.factor.launcher.util.Constants.PACKAGE_NAME;

public class WelcomeActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks
{
    private final String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE};

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

    private ActivityWelcomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (isFirstTime())
        {
            binding = DataBindingUtil.setContentView(this, R.layout.activity_welcome);
            initializeComponents();
        }
        else
            toHomeScreen();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms)
    {
        binding.turnOnBlurButton.setText(R.string.permission_granted);
        binding.skipButton.setText(R.string.next);
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (isFirstTime())
        {
            if(isNotificationServiceEnabled())
            {
                binding.turnOnNotificationServiceButton.setText("Enabled");
                binding.skipNotificationServiceButton.setText("Next");
            }
            else
            {
                binding.turnOnNotificationServiceButton.setText("Enable");
                binding.skipNotificationServiceButton.setText("Skip");
            }
        }
        else
            toHomeScreen();

    }



    //initialize components
    private void initializeComponents()
    {
        binding.welcomeHomePager.addView(binding.welcomePage, 0);
        binding.welcomeHomePager.addView(binding.blurPage, 1);
        binding.welcomeHomePager.addView(binding.notificationPage, 2);
        binding.welcomeHomePager.addView(binding.finishPage, 3);

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            binding.turnOnBlurButton.setText(R.string.permission_granted);
            binding.skipButton.setText(R.string.next);
        }

        //check and request for notification access
        if(isNotificationServiceEnabled())
        {
            binding.turnOnNotificationServiceButton.setText(R.string.enabled);
            binding.skipNotificationServiceButton.setText(R.string.next);
        }

        binding.turnOnBlurButton.setTranslationY(500f);
        binding.skipButton.setTranslationY(500f);
        binding.blurDialog.setupWith(binding.scrollerBase)
                .setBlurAlgorithm(new RenderScriptBlur(this))
                .setBlurRadius(15f)
                .setBlurAutoUpdate(true)
                .setHasFixedTransformationMatrix(false);
        binding.trans.setupWith(binding.scrollerBase)
            .setBlurAlgorithm(new RenderScriptBlur(this))
            .setBlurRadius(15f)
            .setBlurAutoUpdate(true)
            .setHasFixedTransformationMatrix(false);


        binding.welcomeHomePager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
                float xOffset = position + positionOffset;
                binding.arrowButton.setRotation(+180 * xOffset - 180);
                binding.scroller.scrollTo((int) (500*xOffset), 0);
            }

            @Override
            public void onPageSelected(int position)
            {
                if (position == 0)
                    binding.arrowButton.setRotation(180);

                if (position == 1)
                {
                    ObjectAnimator.ofFloat(binding.turnOnBlurButton, "translationY", 0f).setDuration(300).start();
                    ObjectAnimator.ofFloat(binding.skipButton, "translationY", 0f).setDuration(300).start();
                }
                else
                {
                    ObjectAnimator.ofFloat(binding.turnOnBlurButton, "translationY", 500f).setDuration(300).start();
                    ObjectAnimator.ofFloat(binding.skipButton, "translationY", 500f).setDuration(300).start();
                }

                if (position == 2)
                {
                    binding.dimBackground.animate().alpha(1f).setDuration(500).start();
                    ObjectAnimator.ofFloat(binding.turnOnNotificationServiceButton, "translationY", 0f).setDuration(300).start();
                    ObjectAnimator.ofFloat(binding.skipNotificationServiceButton, "translationY", 0f).setDuration(300).start();
                }
                else
                {
                    binding.dimBackground.animate().alpha(0f).setDuration(500).start();
                    ObjectAnimator.ofFloat(binding.turnOnNotificationServiceButton, "translationY", 500f).setDuration(300).start();
                    ObjectAnimator.ofFloat(binding.skipNotificationServiceButton, "translationY", 500f).setDuration(300).start();
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    //check if first time launch
    private boolean isFirstTime()
    {
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        return  !preferences.getBoolean(PACKAGE_NAME + "_RanBefore", false);
    }

    //request storage permission
    public void requestPermission(View view)
    {
        EasyPermissions.requestPermissions(
                new PermissionRequest.Builder(this, Constants.STORAGE_PERMISSION_CODE, perms)
                        .setRationale("Factor launcher needs to access your external storage")
                        .setPositiveButtonText("Okay")
                        .setNegativeButtonText("Cancel")
                        .setTheme(R.style.Theme_AppCompat_Dialog_Alert)
                        .build());


    }

    //request notification access
    public void enableNotificationAccess(View view)
    {
        AlertDialog enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
        enableNotificationListenerAlertDialog.show();
    }

    //move to the next screen
    public void skip(View view)
    {
        binding.welcomeHomePager.setCurrentItem(binding.welcomeHomePager.getCurrentItem() + 1, true);
    }

    //the user has finished setting up, move to home screen
    public void allSet(View view)
    {
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PACKAGE_NAME + "_RanBefore", true);
        editor.commit();
        toHomeScreen();
    }

    //check if notification service is enabled
    private boolean isNotificationServiceEnabled()
    {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(), ENABLED_NOTIFICATION_LISTENERS);
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
    private AlertDialog buildNotificationServiceAlertDialog()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Factor Notification Service");
        alertDialogBuilder.setMessage("Please allow Factor Launcher to access your notifications");
        alertDialogBuilder.setPositiveButton("Ok",
                (dialog, id) -> startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS)));
        alertDialogBuilder.setNegativeButton("Cancel",
                (dialog, id) ->
                {
                    // If you choose to not enable the notification listener
                    // the app. will not work as expected
                });
        return(alertDialogBuilder.create());
    }

    //go to home screen
    private void toHomeScreen()
    {
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(intent);
    }
}