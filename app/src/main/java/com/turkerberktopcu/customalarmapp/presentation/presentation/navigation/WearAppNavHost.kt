package com.turkerberktopcu.customalarmapp.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.turkerberktopcu.customalarmapp.presentation.AlarmEditScreen
import com.turkerberktopcu.customalarmapp.presentation.AlarmListScreen
import com.turkerberktopcu.customalarmapp.presentation.AlarmSettingsScreen

sealed class Screen(val route: String) {
    object AlarmList : Screen("alarm_list")
    object AlarmEdit : Screen("alarm_edit")
    object AlarmSettings : Screen("alarm_settings")
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun WearAppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.AlarmList.route) {
        composable(Screen.AlarmList.route) {
            AlarmListScreen(navController = navController)
        }
        composable(Screen.AlarmEdit.route) {
            AlarmEditScreen(navController = navController)
        }
        composable(Screen.AlarmSettings.route) {
            AlarmSettingsScreen(navController = navController)
        }
    }
}
