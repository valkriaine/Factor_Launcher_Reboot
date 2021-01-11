package com.factor.launcher.managers;

import android.content.Context;
import com.factor.launcher.database.AppSettingsDao;
import com.factor.launcher.database.AppSettingsDatabase;
import com.factor.launcher.models.AppSettings;

public class AppSettingsManager
{
    public boolean areSettingsChanged = false;

    private AppSettings appSettings;

    private final AppSettingsDao daoReference;

    private static AppSettingsManager instance;

    //singleton reference
    public static AppSettingsManager getInstance(Context context)
    {
        if (instance == null)
            instance = new AppSettingsManager(context);
        return instance;
    }

    private AppSettingsManager(Context context)
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

    public void respondToSettingsChange()
    {
        areSettingsChanged = false;
    }

    private void notifySettingsChanged()
    {
        this.areSettingsChanged = true;
    }

    public AppSettings getAppSettings()
    {
        return appSettings;
    }

    public void updateSettings()
    {
        daoReference.updateSettings(appSettings);
        notifySettingsChanged();
    }
}
