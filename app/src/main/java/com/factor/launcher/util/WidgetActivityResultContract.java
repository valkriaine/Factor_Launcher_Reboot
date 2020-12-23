package com.factor.launcher.util;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class WidgetActivityResultContract extends ActivityResultContract<Intent, Intent>
{
    private int requestCode = -1;

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, Intent input)
    {
        requestCode = input.getIntExtra(Constants.WIDGET_KEY, -1);
        return input;
    }

    @Override
    public Intent parseResult(int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent intent)
    {
        if (intent != null)
        {
            Log.d("result_widget", "intent contract: " + requestCode);
            intent.putExtra(Constants.WIDGET_RESULT_KEY, resultCode);
            intent.putExtra(Constants.WIDGET_KEY, requestCode);
            return intent;
        }
        else return new Intent();
    }
}
