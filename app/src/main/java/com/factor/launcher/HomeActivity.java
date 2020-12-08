package com.factor.launcher;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.databinding.DataBindingUtil;
import com.factor.launcher.databinding.ActivityHomeBinding;
import com.factor.launcher.fragments.HomeScreenFragment;

public class HomeActivity extends AppCompatActivity
{
    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);

        if (savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction().setReorderingAllowed(true)
                    .add(R.id.home_fragment_container, HomeScreenFragment.class, null)
                    .commit();
        }
    }
}