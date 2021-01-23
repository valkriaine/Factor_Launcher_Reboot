package com.factor.launcher.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import com.factor.launcher.R;
import com.factor.launcher.databinding.ActivityHomeBinding;
import com.factor.launcher.fragments.HomeScreenFragment;
import com.factor.launcher.view_models.AppSettingsManager;
import com.factor.launcher.util.OnBackPressedCallBack;
import com.factor.launcher.util.Util;

import static com.factor.launcher.util.Constants.PACKAGE_NAME;

public class HomeActivity extends AppCompatActivity implements LifecycleOwner
{

    private Drawable wallpaper = null;

    private WallpaperManager wm;

    private boolean isWallpaperChanged = false;

    private boolean areSettingsChanged = false;

    private boolean isVisible = true;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);

        ActivityHomeBinding binding = ActivityHomeBinding.inflate(getLayoutInflater());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        {
            getWindow().setDecorFitsSystemWindows(false);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);


        setContentView(binding.getRoot());

        //initialize variables to detect wallpaper changes
        wm = WallpaperManager.getInstance(this);
        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            if (wm.getWallpaperInfo() == null) wallpaper = wm.getFastDrawable();
        }



        if (savedInstanceState == null)
            getSupportFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.home_fragment_container, HomeScreenFragment.class, null)
                    .addToBackStack(null)
                    .commit();



        //first launch, start welcome activity
        if (isFirstTime())
        {
            final Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
        }

    }

    @Override
    public void onBackPressed()
    {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.home_fragment_container);
        if (!(fragment instanceof OnBackPressedCallBack) || !((OnBackPressedCallBack) fragment).onBackPressed()) super.onBackPressed();
    }

    //if wallpaper is changed, reload fragment
    @Override
    protected void onResume()
    {
        super.onResume();

        //first launch, start welcome activity
        if (isFirstTime())
        {
            final Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
            return;
        }

        isVisible = true;

        detectWallpaperChanges();

        if(isWallpaperChanged || areSettingsChanged)
        {
            Log.d("settings_changed", "resume");
            isWallpaperChanged = false;
            areSettingsChanged = false;
            reloadFragment();
        }

    }

    //set isVisible to false when activity is no longer visible
    @Override
    protected void onPause()
    {
        super.onPause();
        isVisible = false;
    }

    //perform home button action if activity is visible
    public boolean isVisible()
    {
        return isVisible;
    }

    //detect if wallpaper has changed
    private void detectWallpaperChanges()
    {

        if (wm == null)
            wm = WallpaperManager.getInstance(this);

        //if storage permission is not granted, fall back to live wallpaper
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            //live wallpaper
            if (wm.getWallpaperInfo() != null)
            {
                isWallpaperChanged = wallpaper != null;
                wallpaper = null;
            }
            else //static wallpaper
            {
                isWallpaperChanged = wallpaper == null || !Util.INSTANCE.bytesEqualTo(wallpaper, wm.getFastDrawable());
                wallpaper = wm.getFastDrawable();
            }
        }
    }


    //reload fragment after app settings have changed
    public void reload()
    {
        AppSettingsManager.getInstance(getApplication()).respondToSettingsChange();

        if (isVisible)
        {
            areSettingsChanged = false;
            Log.d("settings_changed", "reload");
            reloadFragment();
        }
        else areSettingsChanged = true; //this activity is paused, reload when resumed
    }

    private void reloadFragment()
    {
        getSupportFragmentManager().popBackStack();
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.home_fragment_container, HomeScreenFragment.class, null)
                .addToBackStack(null)
                .commit();
    }

    //check if first time launch
    private boolean isFirstTime()
    {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
        return  !preferences.getBoolean(PACKAGE_NAME + "_RanBefore", false);
    }

}