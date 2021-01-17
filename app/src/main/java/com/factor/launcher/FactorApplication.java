package com.factor.launcher;

import android.app.Application;
import com.factor.launcher.managers.AppSettingsManager;
import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraLimiter;
import org.acra.annotation.AcraMailSender;
import org.acra.annotation.AcraNotification;
import org.acra.data.StringFormat;


import static org.acra.ReportField.*;

@AcraCore(reportFormat= StringFormat.KEY_VALUE_LIST,  reportContent = {ANDROID_VERSION, PHONE_MODEL, STACK_TRACE, LOGCAT})
@AcraNotification(
        resText = R.string.crash_text,
        resTitle = R.string.crash_report,
        resChannelName = R.string.channel_name)
@AcraMailSender(mailTo = "valkriaine@hotmail.com")
@AcraLimiter
public class FactorApplication extends Application
{
    //add other services here

    @Override
    public void onCreate()
    {
        super.onCreate();
        ACRA.init(this);

        //initialize settings
        AppSettingsManager.getInstance(getApplicationContext());
    }
}
