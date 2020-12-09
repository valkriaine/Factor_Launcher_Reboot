package com.factor.launcher;

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

        if (savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction().setReorderingAllowed(true)
                    .add(R.id.home_fragment_container, HomeScreenFragment.class, null)
                    .commit();
        }
    }
}