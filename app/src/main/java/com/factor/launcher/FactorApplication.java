package com.factor.launcher;

import android.app.Application;
import com.factor.launcher.managers.AppSettingsManager;

public class FactorApplication extends Application
{
    //add other services here

    @Override
    public void onCreate()
    {
        super.onCreate();

        //initialize settings
        AppSettingsManager.getInstance(getApplicationContext());

        //todo: remove this for release
        try {
            Class.forName("dalvik.system.CloseGuard")
                    .getMethod("setEnabled", boolean.class)
                    .invoke(null, true);
        } catch (ReflectiveOperationException e)
        {
            throw new RuntimeException(e);
        }
    }
}
