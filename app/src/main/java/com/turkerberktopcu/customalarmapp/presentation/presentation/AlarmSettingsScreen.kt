package com.turkerberktopcu.customalarmapp.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController

@Composable
fun AlarmSettingsScreen(navController: NavController) {
    var isSoundOn by remember { mutableStateOf(true) }
    var isVibrationOn by remember { mutableStateOf(true) }
    var isSnoozeEnabled by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Scaffold(
        timeText = { TimeText() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // Geri butonu (Eğer bu ekran top-level değilse)
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Alarm Ayarları",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(text = "Alarm Sesi", modifier = Modifier.weight(1f))
                Switch(
                    checked = isSoundOn,
                    onCheckedChange = { isSoundOn = it }
                )
            }
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(text = "Titreşim", modifier = Modifier.weight(1f))
                Switch(
                    checked = isVibrationOn,
                    onCheckedChange = { isVibrationOn = it }
                )
            }
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(text = "Ertelenmiş (Snooze)", modifier = Modifier.weight(1f))
                Switch(
                    checked = isSnoozeEnabled,
                    onCheckedChange = { isSnoozeEnabled = it }
                )
            }
        }
    }
}

@Preview(device = "id:wearos_small_round", showSystemUi = true)
@Composable
fun AlarmSettingsScreenPreview() {
    AlarmSettingsScreen(navController = rememberNavController())
}
