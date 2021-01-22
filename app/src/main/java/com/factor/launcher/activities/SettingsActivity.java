package com.factor.launcher.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.lifecycle.LifecycleOwner;
import com.factor.launcher.R;
import com.factor.launcher.fragments.SettingsFragment;

import static com.factor.launcher.util.Constants.SYSTEM_DIALOG_REASON_HOME_KEY;
import static com.factor.launcher.util.Constants.SYSTEM_DIALOG_REASON_KEY;


public class SettingsActivity extends AppCompatActivity implements LifecycleOwner
{
    private HomeButtonPressReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        receiver = new HomeButtonPressReceiver(this);

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("Options");


        getSupportFragmentManager().popBackStack();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_fragment_container, SettingsFragment.class, null)
                .commit();


        registerReceiver(receiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (receiver != null) unregisterReceiver(receiver);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
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
            if (reason != null && reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) activity.onBackPressed();
        }
    }
}