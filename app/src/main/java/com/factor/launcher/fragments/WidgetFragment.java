package com.factor.launcher.fragments;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.factor.launcher.R;
import com.factor.launcher.databinding.FragmentWidgetBinding;

public class WidgetFragment extends Fragment
{
    private static final int APPWIDGET_HOST_ID = 1024;

    private AppWidgetManager appWidgetManager;

    private AppWidgetHost appWidgetHost;

    public WidgetFragment()
    {
        // Required empty public constructor
    }

    public static WidgetFragment newInstance()
    {
        return new WidgetFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_widget, container, false);
    }


    private void initializeComponents()
    {
        appWidgetManager = AppWidgetManager.getInstance(requireContext());


    }
}