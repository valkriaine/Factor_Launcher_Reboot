package com.factor.launcher.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.content.PermissionChecker;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import com.factor.launcher.R;
import com.factor.launcher.databinding.ActivityHomeBinding;
import com.factor.launcher.fragments.HomeScreenFragment;
import com.factor.launcher.util.OnSystemActionsCallBack;
import com.factor.launcher.util.Util;
import com.factor.launcher.view_models.AppSettingsManager;

import static com.factor.launcher.util.Constants.PACKAGE_NAME;

public class HomeActivity extends AppCompatActivity implements LifecycleOwner
{

    private Drawable wallpaper = null;

    private int wallpaperId = 0;

    private WallpaperManager wm;

    private boolean isWallpaperChanged = false;

    private boolean areSettingsChanged = false;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        super.onCreate(savedInstanceState);

        ActivityHomeBinding binding = ActivityHomeBinding.inflate(getLayoutInflater());

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(binding.getRoot());

        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //initialize variables to detect wallpaper changes
        wm = WallpaperManager.getInstance(this);
        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED)
        {
            if (wm.getWallpaperInfo() == null)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    wallpaperId = wm.getWallpaperId(WallpaperManager.FLAG_SYSTEM);
                else
                if (wm.getWallpaperInfo() == null) wallpaper = wm.getFastDrawable();
            }
        }


        if (savedInstanceState == null)
            getSupportFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.home_fragment_container, new HomeScreenFragment(), null)
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
        if (!(fragment instanceof OnSystemActionsCallBack) || !((OnSystemActionsCallBack) fragment).onBackPressed()) finishAfterTransition();
    }

    //if wallpaper is changed, reload fragment
    @Override
    protected void onResume()
    {
        super.onResume();

        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //first launch, start welcome activity
        if (isFirstTime())
        {
            final Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
            return;
        }

        detectWallpaperChanges();

        if(isWallpaperChanged || areSettingsChanged)
        {
            isWallpaperChanged = false;
            areSettingsChanged = false;
            reloadFragment();
        }
    }


    //home button press
    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        if (hasWindowFocus())
        {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.home_fragment_container);
            if (!(fragment instanceof OnSystemActionsCallBack) || !((OnSystemActionsCallBack) fragment).onNewIntent())
                finishAfterTransition();
        }
    }


    //detect if wallpaper has changed
    private void detectWallpaperChanges()
    {

        if (wm == null)
            wm = WallpaperManager.getInstance(this);

        //if storage permission is not granted, fall back to live wallpaper
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==PackageManager.PERMISSION_GRANTED)
            {
                //live wallpaper
                if (wm.getWallpaperInfo() != null)
                {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    {
                        isWallpaperChanged = wallpaperId != 0;
                        wallpaperId = 0;
                    }
                    else
                    {
                        isWallpaperChanged = wallpaper != null;
                        wallpaper = null;
                    }
                }
                else //static wallpaper
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    {
                        isWallpaperChanged = wallpaperId == 0 || wallpaperId != wm.getWallpaperId(WallpaperManager.FLAG_SYSTEM);
                        wallpaperId = wm.getWallpaperId(WallpaperManager.FLAG_SYSTEM);
                    }
                    else
                    {
                        isWallpaperChanged = wallpaper == null || !Util.bytesEqualTo(wallpaper, wm.getFastDrawable());
                        wallpaper = wm.getFastDrawable();
                    }
                }
            }
        }
    }


    //reload fragment after app settings have changed
    public void reload()
    {
        AppSettingsManager.getInstance(getApplication()).respondToSettingsChange();

        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
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