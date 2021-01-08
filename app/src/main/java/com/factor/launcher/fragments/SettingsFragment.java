package com.factor.launcher.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.factor.launcher.databinding.FragmentSettingsBinding;
import eightbitlab.com.blurview.RenderScriptBlur;


public class SettingsFragment extends Fragment
{
    private FragmentSettingsBinding binding;

    public SettingsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(getLayoutInflater());
        initializeComponents();
        return binding.getRoot();
    }


    //todo: add more ui components
    private void initializeComponents()
    {
        binding.demoBlur.setupWith(binding.demoBackground)
                .setBlurAlgorithm(new RenderScriptBlur(requireContext()))
                .setBlurRadius(15f)
                .setBlurAutoUpdate(true)
                .setHasFixedTransformationMatrix(true);
    }

}