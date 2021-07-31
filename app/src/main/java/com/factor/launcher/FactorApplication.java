package com.factor.launcher;

import android.app.Application;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import com.factor.launcher.util.Constants;
import com.factor.launcher.view_models.AppSettingsManager;
import org.acra.ACRA;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.NotificationConfigurationBuilder;
import org.acra.data.StringFormat;

public class FactorApplication extends Application
{
    private static AppWidgetHost appWidgetHost;
    private static AppWidgetManager appWidgetManager;

    @Override
    public void onCreate()
    {
        super.onCreate();
        CoreConfigurationBuilder builder = new CoreConfigurationBuilder(this);
        //core configuration:
        builder
                .withBuildConfigClass(BuildConfig.class)
                .withReportFormat(StringFormat.JSON);
        builder.getPluginConfigurationBuilder(NotificationConfigurationBuilder.class)
                //required
                .withEnabled(true)
                //required
                .withResTitle(R.string.crash_report)
                //required
                .withResText(R.string.crash_text)
                //required
                .withResChannelName(R.string.channel_name)
                //defaults to android.R.string.ok
                .withResSendButtonText(R.string.okay)
                //defaults to android.R.string.cancel
                .withResDiscardButtonText(R.string.cancel)
                //defaults to false
                .withSendOnClick(false);

        ACRA.init(this, builder);

        //initialize settings
        AppSettingsManager.getInstance(this);

        appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        appWidgetHost = new AppWidgetHost(getApplicationContext(), Constants.WIDGET_HOST_ID);
        appWidgetHost.startListening();
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
}
