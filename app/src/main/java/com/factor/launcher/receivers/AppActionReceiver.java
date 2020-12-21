package com.factor.launcher.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import com.factor.launcher.activities.HomeActivity;
import com.factor.launcher.databinding.FragmentHomeScreenBinding;
import com.factor.launcher.managers.AppListManager;
import com.factor.launcher.util.Constants;

import java.util.Objects;

//handle tile pinning/unpinning, app editing, and home button press
public class AppActionReceiver extends BroadcastReceiver
{
    private static final String SYSTEM_DIALOG_REASON_KEY = "reason";

    private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

    private AppListManager appListManager;

    private  FragmentHomeScreenBinding binding;

    public AppActionReceiver(AppListManager appListManager, FragmentHomeScreenBinding binding)
    {
        this.appListManager = appListManager;
        this.binding = binding;
    }

    public void invalidate()
    {
        this.binding = null;
        this.appListManager = null;
    }


    @Override
    public void onReceive(Context context, Intent intent)
    {

        switch (intent.getAction())
        {
            //a factor has been unpinned, change its pinned state in the app drawer
            case Constants.BROADCAST_ACTION_REMOVE:
                String packageName;
                packageName = intent.getStringExtra(Constants.REMOVE_KEY);
                appListManager.unPin(packageName);
                break;

            //a factor has been pinned, scroll to home screen and location the newly pinned tile
            case Constants.BROADCAST_ACTION_ADD:
                binding.homePager.setCurrentItem(0, true);
                Objects.requireNonNull(binding.tilesList.getLayoutManager())
                        .smoothScrollToPosition(binding.tilesList, new RecyclerView.State(), intent.getIntExtra(Constants.ADD_KEY, 0));
                break;

            //an app has been renamed, scroll to the new position of the app
            case Constants.BROADCAST_ACTION_RENAME:
                Objects.requireNonNull(binding.appsList.getLayoutManager())
                        .smoothScrollToPosition(binding.appsList, new RecyclerView.State(), intent.getIntExtra(Constants.RENAME_KEY, 0));
                break;

            //the home button has been pressed
            case Intent.ACTION_CLOSE_SYSTEM_DIALOGS:
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason != null) {
                    if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY) && ((HomeActivity)appListManager.getActivity()).isVisible())
                    {
                        // home button short press
                        binding.homePager.setCurrentItem(0, true);
                        Objects.requireNonNull(binding.appsList.getLayoutManager()).scrollToPosition(0);
                        Objects.requireNonNull(binding.tilesList.getLayoutManager())
                                .smoothScrollToPosition(binding.tilesList, new RecyclerView.State(), 0);
                    }
                }
                break;
        }
    }
}
