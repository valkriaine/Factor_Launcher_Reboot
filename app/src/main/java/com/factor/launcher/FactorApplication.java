package com.factor.launcher;

import android.app.Application;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.Context;

import com.factor.launcher.util.Constants;
import com.factor.launcher.view_models.AppSettingsManager;
import org.acra.ACRA;

import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.MailSenderConfigurationBuilder;

import org.acra.config.ToastConfigurationBuilder;
import org.acra.data.StringFormat;


public class FactorApplication extends Application
{
    private static AppWidgetHost appWidgetHost;
    private static AppWidgetManager appWidgetManager;

    @Override
    public void onCreate()
    {
        super.onCreate();
        ACRA.init(this);

        //initialize settings
        AppSettingsManager.getInstance(this);

        appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        appWidgetHost = new AppWidgetHost(getApplicationContext(), Constants.WIDGET_HOST_ID);
        //appWidgetHost.startListening();
    }

    @Override
    public void onTerminate()
    {
        super.onTerminate();

        appWidgetHost.stopListening();
        appWidgetHost = null;
    }

    public static AppWidgetHost getAppWidgetHost() { return appWidgetHost; }

    public static AppWidgetManager getAppWidgetManager() { return appWidgetManager; }

    @Override
    protected void attachBaseContext(Context base)
    {
        super.attachBaseContext(base);
        ACRA.init(this, new CoreConfigurationBuilder()
                //core configuration:
                .withBuildConfigClass(BuildConfig.class)
                .withReportFormat(StringFormat.JSON)
                .withPluginConfigurations(
                        new MailSenderConfigurationBuilder()
                                //required
                                .withMailTo("valkriaine@hotmail.com")
                                //defaults to true
                                .withReportAsFile(true)
                                //defaults to ACRA-report.stacktrace
                                .build()
                )
        );


    }
}
