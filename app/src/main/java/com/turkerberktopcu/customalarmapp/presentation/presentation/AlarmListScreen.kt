package com.turkerberktopcu.customalarmapp.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import com.turkerberktopcu.customalarmapp.presentation.alarm.Alarm
import com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmManager
import com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmScheduler
import com.turkerberktopcu.customalarmapp.presentation.navigation.Screen
@Composable
fun AlarmListScreen(navController: NavController) {
    val context = LocalContext.current
    val alarmManager = remember { AlarmManager(context) }
    val alarmScheduler = remember { AlarmScheduler(context) }
    // Use a state list for alarms
    val alarms = remember { mutableStateListOf<Alarm>().apply { addAll(alarmManager.getAllAlarms()) } }

    Scaffold(timeText = { TimeText() }) {
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
                items(items = alarms, key = { it.id }) { alarm ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${alarm.hour.toString().padStart(2, '0')}:${alarm.minute.toString().padStart(2, '0')}",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = alarm.label,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Switch(
                            checked = alarm.isEnabled,
                            onCheckedChange = { newValue ->
                                alarmManager.toggleAlarm(alarm.id)
                                if (newValue) {
                                    alarmScheduler.scheduleAlarm(
                                        alarm.id,
                                        alarm.timeInMillis,
                                        alarm.label
                                    )
                                } else {
                                    alarmScheduler.cancelAlarm(alarm.id)
                                }
                                // Update our state list by clearing and adding fresh data
                                alarms.clear()
                                alarms.addAll(alarmManager.getAllAlarms())
                            }
                        )
                        IconButton(
                            onClick = {
                                alarmManager.deleteAlarm(alarm.id)
                                alarmScheduler.cancelAlarm(alarm.id)
                                alarms.clear()
                                alarms.addAll(alarmManager.getAllAlarms())
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Sil",
                                tint = Color.White
                            )
                        }
                    }
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