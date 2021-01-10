package com.factor.launcher.managers;

import android.content.Context;
import com.factor.launcher.database.AppSettingsDatabase;
import com.factor.launcher.models.AppSettings;
import com.factor.launcher.util.Constants;

public class AppSettingsManager
{
    private final AppSettings appSettings;

    public AppSettingsManager(Context context)
    {
        appSettings = AppSettingsDatabase.Companion.getInstance(context).appSettingsDao().retrieveSettings(Constants.PACKAGE_NAME);
    }

    public AppSettings getAppSettings()
    {
        return appSettings;
    }
}
