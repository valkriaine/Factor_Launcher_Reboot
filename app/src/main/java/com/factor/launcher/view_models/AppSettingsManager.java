package com.factor.launcher.view_models;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import com.factor.launcher.database.AppSettingsDatabase;
import com.factor.launcher.models.AppSettings;

public class AppSettingsManager extends AndroidViewModel
{
    public boolean areSettingsChanged = false;

    private AppSettings appSettings;

    private final AppSettingsDatabase.AppSettingsDao daoReference;

    private static AppSettingsManager instance;

    //todo: implement LiveData here
    //singleton reference
    public static AppSettingsManager getInstance(Application application)
    {
        if (instance == null)
            instance = new AppSettingsManager(application);
        return instance;
    }

    private AppSettingsManager(Application application)
    {
        super(application);
        daoReference = AppSettingsDatabase.Companion.getInstance(application.getApplicationContext()).appSettingsDao();
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

    public AppSettingsManager setAppSettings(AppSettings settings)
    {
        this.appSettings = settings;
        return this;
    }

    public void updateSettings()
    {
        daoReference.updateSettings(appSettings);
        notifySettingsChanged();
    }
}
