package com.jintonic.app.data.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.jintonic.app.core.AppConstants

// Wrist biofeedback: one long pulse = inhale, two short pulses = exhale.
class Haptics(context: Context) {

    private val vibrator: Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Vibrator::class.java)
        }

    fun inhale() {
        vibrator?.vibrate(VibrationEffect.createOneShot(AppConstants.INHALE_PULSE_MS, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun exhale() {
        val pulse = AppConstants.EXHALE_PULSE_MS
        val timings = longArrayOf(0, pulse, AppConstants.EXHALE_GAP_MS, pulse)
        vibrator?.vibrate(VibrationEffect.createWaveform(timings, -1))
    }

    fun tick() {
        vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
    }
}
