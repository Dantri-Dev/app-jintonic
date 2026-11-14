package com.jintonic.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.jintonic.app.data.sensors.VitalsMonitor
import com.jintonic.app.ui.navigation.AppNavHost
import com.jintonic.app.ui.theme.FfdTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val vitals: VitalsMonitor by inject()

    private val permissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        // Denied (or permanently denied): HR falls back to simulation, labelled in Parametri.
        vitals.start(sensorPermitted = result[heartRatePermission()] == true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FfdTheme { AppNavHost() }
        }
        if (hasHeartRatePermission()) {
            vitals.start(sensorPermitted = true)
        } else {
            permissions.launch(arrayOf(heartRatePermission(), Manifest.permission.POST_NOTIFICATIONS))
        }
    }

    override fun onDestroy() {
        if (isFinishing) vitals.stop()
        super.onDestroy()
    }

    private fun hasHeartRatePermission() =
        ContextCompat.checkSelfPermission(this, heartRatePermission()) == PackageManager.PERMISSION_GRANTED

    // Wear OS 6 (API 36) split BODY_SENSORS into granular health permissions.
    private fun heartRatePermission(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) READ_HEART_RATE else Manifest.permission.BODY_SENSORS

    private companion object {
        const val READ_HEART_RATE = "android.permission.health.READ_HEART_RATE"
    }
}
