package com.factor.launcher.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.factor.launcher.managers.AppListManager;
import com.factor.launcher.models.UserApp;

import java.util.List;

//listen for app update, install, uninstall, and other changes related to the app drawer
public class PackageActionsReceiver extends BroadcastReceiver
{
    private AppListManager appListManager;

    public PackageActionsReceiver(AppListManager appListManager)
    {
        this.appListManager = appListManager;
    }

    public void invalidate()
    {
        this.appListManager.invalidate();
        this.appListManager = null;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        switch (action)
        {
            case Intent.ACTION_PACKAGE_ADDED: //a new app (package) is installed
                {
                UserApp app = new UserApp();
                app.setPackageName(intent.getData().getSchemeSpecificPart());
                if (!intent.getBooleanExtra(Intent.EXTRA_REPLACING, false))
                    appListManager.addApp(app); //the new app is not in the list
                else
                {
                    appListManager.updateApp(app); //the new app already exists in the list
                }
                break;
            }
            case Intent.ACTION_PACKAGE_REMOVED: //an app (package) has been uninstalled
                if (!intent.getBooleanExtra(Intent.EXTRA_REPLACING, false))
                {
                    UserApp app = new UserApp();
                    app.setPackageName(intent.getData().getSchemeSpecificPart());
                    appListManager.removeApp(app);
                }
                break;
            case Intent.ACTION_PACKAGE_CHANGED: //an app (package) has been changed
                {
                UserApp app = new UserApp();
                app.setPackageName(intent.getData().getSchemeSpecificPart());
                appListManager.updateApp(app);
                break;
            }

            //todo: need to test
            case Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE: //an app (package) installed on the external storage is reconnected to the device
                {
                List<String> packages = intent.getStringArrayListExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
                for (String p : packages) {
                    UserApp app = new UserApp();
                    if (p.contains("package:")) {
                        p = p.replace("package:", "");
                    }
                    app.setPackageName(p);
                    appListManager.addApp(app);
                }
                break;
            }

            //todo: need to test
            case Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE: //an app (package) installed on the external storage is disconnected from the device
                {
                List<String> packages = intent.getStringArrayListExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
                for (String p : packages)
                {
                    UserApp app = new UserApp();
                    if (p.contains("package:"))
                    {
                        p = p.replace("package:", "");
                    }
                    app.setPackageName(p);
                    appListManager.removeApp(app);
                }
                break;
            }
        }
    }
}
