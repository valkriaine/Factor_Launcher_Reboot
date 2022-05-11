package com.factor.launcher.ui.wave_animation

import android.graphics.Color
import com.factor.launcher.ui.wave_animation.WaveView.WaveData

object Waves
{
    @JvmStatic
    fun generateWave() : WaveData
    {
        return WaveData(
            (800 + Math.random() * 100).toFloat(),
            (100 + Math.random() * 20).toFloat(),
            (200 + Math.random() * 20).toFloat(),
            (Math.random() * 50).toFloat(),
            Color.WHITE,
            Color.BLACK,
            0.3f,
            (2000 + Math.random() * 1000).toLong(),
            true)
        }
}
