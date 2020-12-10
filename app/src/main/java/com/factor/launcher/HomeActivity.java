package com.factor.launcher;

import android.view.Window;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import com.factor.launcher.fragments.HomeScreenFragment;
import com.factor.launcher.util.OnBackPressedCallBack;

public class HomeActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_home);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);



        if (savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction().setReorderingAllowed(true)
                    .add(R.id.home_fragment_container, HomeScreenFragment.class, null)
                    .commit();
        }
    }

    @Override
    public void onBackPressed()
    {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.home_fragment_container);
        if (!(fragment instanceof OnBackPressedCallBack) || !((OnBackPressedCallBack) fragment).onBackPressed())
        {
            super.onBackPressed();
        }
    }
}