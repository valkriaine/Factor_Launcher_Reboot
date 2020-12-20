package com.factor.launcher.util;

import android.content.Context;
import android.content.Intent;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class WidgetActivityResultContract extends ActivityResultContract<Intent, Intent>
{
    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, Intent input)
    {
        return input;
    }

    @Override
    public Intent parseResult(int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent intent)
    {
        if (intent != null)
        {
            return intent.putExtra(Constants.WIDGET_RESULT_KEY, resultCode);
        }
        else return new Intent();
    }
}
