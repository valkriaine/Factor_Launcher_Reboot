package com.factor.launcher;

import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.factor.launcher.fragments.HomeScreenFragment;

public class HomeActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);

        if (savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction().setReorderingAllowed(true)
                    .add(R.id.home_fragment_container, HomeScreenFragment.class, null)
                    .commit();
        }
    }
}