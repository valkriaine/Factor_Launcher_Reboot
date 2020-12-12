package com.factor.launcher;

import android.content.ComponentName;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import com.factor.launcher.fragments.HomeScreenFragment;
import com.factor.launcher.util.OnBackPressedCallBack;

import static android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS;

public class HomeActivity extends AppCompatActivity
{

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";


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

        //check and request for notification access
        if(!isNotificationServiceEnabled())
        {
            AlertDialog enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog.show();
        }

        //todo: request external storage access here

    }

    @Override
    public void onBackPressed()
    {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.home_fragment_container);
        if (!(fragment instanceof OnBackPressedCallBack) || !((OnBackPressedCallBack) fragment).onBackPressed()) super.onBackPressed();
    }

    private boolean isNotificationServiceEnabled()
    {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(), ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (String name : names)
            {
                final ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null)
                {
                    if (TextUtils.equals(pkgName, cn.getPackageName()))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    //display alert dialog to request for notification access
    //todo: redesign this
    private AlertDialog buildNotificationServiceAlertDialog()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("notification service title");
        alertDialogBuilder.setMessage("notification service explanation");
        alertDialogBuilder.setPositiveButton("yes",
                (dialog, id) -> startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS)));
        alertDialogBuilder.setNegativeButton("yes",
                (dialog, id) ->
                {
                    // If you choose to not enable the notification listener
                    // the app. will not work as expected
                });
        return(alertDialogBuilder.create());
    }
}