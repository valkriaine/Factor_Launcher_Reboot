package com.factor.launcher.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.factor.launcher.R;
import com.factor.launcher.fragments.SettingsFragment;
import static com.factor.launcher.util.Constants.SYSTEM_DIALOG_REASON_HOME_KEY;
import static com.factor.launcher.util.Constants.SYSTEM_DIALOG_REASON_KEY;


public class SettingsActivity extends AppCompatActivity
{
    private HomeButtonPressReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        receiver = new HomeButtonPressReceiver(this);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_fragment_container, new SettingsFragment())
                .commit();


        registerReceiver(receiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (receiver != null)
            unregisterReceiver(receiver);
    }


    //handle home button press
    private static class HomeButtonPressReceiver extends BroadcastReceiver
    {
        private final Activity activity;

        public HomeButtonPressReceiver(Activity activity)
        {
            this.activity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent)
        {
            String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
            if (reason != null && reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY))
                activity.onBackPressed();
        }
    }
}