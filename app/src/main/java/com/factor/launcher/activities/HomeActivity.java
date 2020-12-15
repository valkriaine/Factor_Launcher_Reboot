package com.factor.launcher.activities;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import com.factor.launcher.R;
import com.factor.launcher.fragments.HomeScreenFragment;
import com.factor.launcher.util.DrawableComparison;
import com.factor.launcher.util.OnBackPressedCallBack;

public class HomeActivity extends AppCompatActivity
{

    private Drawable wallpaper = null;

    private WallpaperManager wm;

    private boolean isWallpaperChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_home);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);


        //initialize variables to detect wallpaper changes
        wm = WallpaperManager.getInstance(this);
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            if (wm.getWallpaperInfo() == null) wallpaper = wm.getFastDrawable();


        if (savedInstanceState == null)
            getSupportFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.home_fragment_container, HomeScreenFragment.class, null)
                    .addToBackStack(null)
                    .commit();


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
        detectWallpaperChanges();

        if(isWallpaperChanged)
        {
            isWallpaperChanged = false;
            getSupportFragmentManager().beginTransaction().setReorderingAllowed(true)
                    .replace(R.id.home_fragment_container, HomeScreenFragment.class, null)
                    .addToBackStack(null)
                    .commit();
        }
    }

    //detect if wallpaper has changed
    private void detectWallpaperChanges()
    {

        if (wm == null)
            wm = WallpaperManager.getInstance(this);

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            //live wallpaper
            if (wm.getWallpaperInfo() != null)
            {
                isWallpaperChanged = wallpaper != null;
                wallpaper = null;
            }
            else
            {
                isWallpaperChanged = wallpaper == null || !DrawableComparison.INSTANCE.bytesEqualTo(wallpaper, wm.getFastDrawable());
                wallpaper = wm.getFastDrawable();
                Log.d("resume", "is wallpaper changed: " + isWallpaperChanged);
            }
        }
    }
}