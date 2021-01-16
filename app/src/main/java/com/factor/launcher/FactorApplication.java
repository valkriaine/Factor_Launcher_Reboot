package com.factor.launcher;

import android.app.Application;
import com.factor.launcher.managers.AppSettingsManager;
import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraMailSender;
import org.acra.annotation.AcraNotification;
import org.acra.data.StringFormat;


@AcraCore(reportFormat= StringFormat.KEY_VALUE_LIST)
@AcraNotification(resText = R.string.crash_text,
        resTitle = R.string.crash_report,
        resChannelName = R.string.channel_name)
@AcraMailSender(mailTo = "valkriaine@hotmail.com")
public class FactorApplication extends Application
{
    //add other services here

    @Override
    public void onCreate()
    {
        super.onCreate();

        //initialize ACRA
        ACRA.init(this);

        //initialize settings
        AppSettingsManager.getInstance(getApplicationContext());

        //todo: remove this for release
        try
        {
            Class.forName("dalvik.system.CloseGuard").getMethod("setEnabled", boolean.class).invoke(null, true);
        }
        catch (ReflectiveOperationException ignored){}
    }
}
