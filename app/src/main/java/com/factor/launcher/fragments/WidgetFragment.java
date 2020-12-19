package com.factor.launcher.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.factor.launcher.R;

public class WidgetFragment extends Fragment
{

    public WidgetFragment()
    {
        // Required empty public constructor
    }

    public static WidgetFragment newInstance(String param1, String param2)
    {
        WidgetFragment fragment = new WidgetFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_widget, container, false);

    }
}