package com.factor.launcher.managers;

import android.content.Context;
import com.factor.launcher.database.AppSettingsDao;
import com.factor.launcher.database.AppSettingsDatabase;
import com.factor.launcher.models.AppSettings;

public class AppSettingsManager
{
    private AppSettings appSettings;

    private final AppSettingsDao daoReference;

    public AppSettingsManager(Context context)
    {
        daoReference = AppSettingsDatabase.Companion.getInstance(context).appSettingsDao();
        appSettings = daoReference.retrieveSettings();

        try
        {
            appSettings.getKey();
        }
        catch (NullPointerException exception)
        {
            appSettings = new AppSettings();
            daoReference.initializeSettings(appSettings);
        }

    }

    public AppSettings getAppSettings()
    {
        return appSettings;
    }

    public void updateSettings()
    {
        daoReference.updateSettings(appSettings);
    }
}
