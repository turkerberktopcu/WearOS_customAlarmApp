package com.turkerberktopcu.customalarmapp.presentation.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.TimeText
import com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmManager
import com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmScheduler
import com.turkerberktopcu.customalarmapp.presentation.navigation.WearAppNavHost
import com.turkerberktopcu.customalarmapp.presentation.theme.CustomAlarmAppTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install the splash screen.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        // Optionally set your theme.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        setTheme(android.R.style.Theme_DeviceDefault)
        setContent {
            CustomAlarmAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Display the current time at the top.
                        TimeText(modifier = Modifier.align(Alignment.TopCenter))
                        // Launch your alarm app's navigation host.
                        WearAppNavHost()
                    }
                }
            }
        }
        val alarmManager = AlarmManager(this)
        val alarmScheduler = AlarmScheduler(this)

        if (alarmManager.isDailyResetEnabled()) {
            alarmScheduler.scheduleDailyReset()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun MainActivityPreview() {
    CustomAlarmAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                TimeText(modifier = Modifier.align(Alignment.TopCenter))
                WearAppNavHost()
            }
        }
    }
}
