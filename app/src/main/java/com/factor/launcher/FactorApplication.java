package com.factor.launcher;

import android.app.Application;


public class FactorApplication extends Application
{
    //add other services here

    @Override
    public void onCreate()
    {
        super.onCreate();

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
