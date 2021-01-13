package com.factor.launcher.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.widget.AppCompatTextView
import com.factor.launcher.R
import com.skydoves.colorpickerview.AlphaTileView
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.flag.FlagView


@SuppressLint("ViewConstructor")
class CustomFlag(context: Context?, layout: Int) : FlagView(context, layout)
{
    private val textView: AppCompatTextView = findViewById(R.id.flag_color_code)
    private val alphaTileView: AlphaTileView = findViewById(R.id.flag_color_layout)

    override fun onRefresh(colorEnvelope: ColorEnvelope)
    {
        textView.text = "#$colorEnvelope.hexCode"
        alphaTileView.setPaintColor(colorEnvelope.color)
    }

}