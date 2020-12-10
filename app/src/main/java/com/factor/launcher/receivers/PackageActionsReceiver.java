package com.factor.launcher.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import com.factor.launcher.managers.AppListManager;
import com.factor.launcher.models.UserApp;

import java.util.List;

public class PackageActionsReceiver extends BroadcastReceiver
{
    private final AppListManager appListManager;

    public PackageActionsReceiver(AppListManager a)
    {
        appListManager = a;

        IntentFilter sdFilter = new IntentFilter();
        sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
        sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);

        a.getActivity().registerReceiver(this, sdFilter);
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        Log.d("receiver", action);
        switch (action) {
            case Intent.ACTION_PACKAGE_ADDED: {
                UserApp app = new UserApp();
                app.setPackageName(intent.getData().getSchemeSpecificPart());
                if (!intent.getBooleanExtra(Intent.EXTRA_REPLACING, false))
                    appListManager.addApp(app);
                else
                    appListManager.updateApp(app);


                break;
            }
            case Intent.ACTION_PACKAGE_REMOVED:
                if (!intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
                    UserApp app = new UserApp();
                    app.setPackageName(intent.getData().getSchemeSpecificPart());
                    appListManager.removeApp(app);
                }
                break;
            case Intent.ACTION_PACKAGE_CHANGED: {
                UserApp app = new UserApp();
                app.setPackageName(intent.getData().getSchemeSpecificPart());
                appListManager.updateApp(app);
                break;
            }

            //todo: need to test
            case Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE: {
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
            case Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE: {
                List<String> packages = intent.getStringArrayListExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
                for (String p : packages) {
                    UserApp app = new UserApp();
                    if (p.contains("package:")) {
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
