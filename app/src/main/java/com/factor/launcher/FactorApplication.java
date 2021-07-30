package com.factor.launcher;

import android.app.Application;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import com.factor.launcher.util.Constants;
import com.factor.launcher.view_models.AppSettingsManager;
import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraLimiter;
import org.acra.annotation.AcraMailSender;
import org.acra.annotation.AcraNotification;
import org.acra.data.StringFormat;


import static org.acra.ReportField.*;

@AcraCore(reportFormat= StringFormat.KEY_VALUE_LIST,  reportContent = {APP_VERSION_CODE, ANDROID_VERSION, PHONE_MODEL, STACK_TRACE, LOGCAT})
@AcraNotification(
        resText = R.string.crash_text,
        resTitle = R.string.crash_report,
        resChannelName = R.string.channel_name)
@AcraMailSender(mailTo = "valkriaine@hotmail.com")
@AcraLimiter
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
