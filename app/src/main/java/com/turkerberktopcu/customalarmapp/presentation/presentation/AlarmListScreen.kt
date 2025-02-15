package com.turkerberktopcu.customalarmapp.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import com.turkerberktopcu.customalarmapp.presentation.navigation.Screen
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController

// Örnek alarm veri modeli
data class AlarmItem(
    val id: Int,
    val time: String,
    val label: String,
    val isEnabled: Boolean
)

// Örnek alarm listesi
fun sampleAlarms() = listOf(
    AlarmItem(1, "07:00 AM", "Sabah Alarmı", true),
    AlarmItem(2, "08:30 AM", "Toplantı Hatırlatıcısı", false),
    AlarmItem(3, "09:45 AM", "Spor Alarmı", true)
)

@Composable
fun AlarmListScreen(navController: NavController, alarms: List<AlarmItem> = sampleAlarms()) {
    /**
     * Wear Compose Scaffold:
     * timeText, positionIndicator, vignette vb. parametreleri destekler
     * ancak content lambda’sı “(BoxScope.() -> Unit)” bekler.
     */
    Scaffold(
        timeText = { TimeText() }
    ) {
        // Scaffold'un content bloğu bir BoxScope açar.
        // paddingValues artık yok, bu nedenle kendiniz margin/padding uygulayabilirsiniz.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Button(
                onClick = { navController.navigate(Screen.AlarmEdit.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Alarm Ekle")
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(alarms) { alarm ->
                    Text(
                        text = "${alarm.time} - ${alarm.label}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Preview(device = "id:wearos_small_round", showSystemUi = true)
@Composable
fun AlarmListScreenPreview() {
    AlarmListScreen(navController = rememberNavController())
}
