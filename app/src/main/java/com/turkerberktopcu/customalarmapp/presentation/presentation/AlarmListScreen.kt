package com.turkerberktopcu.customalarmapp.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material3.IconButton
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.wear.compose.material.*
import com.turkerberktopcu.customalarmapp.presentation.alarm.Alarm
import com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmManager
import com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmScheduler
import com.turkerberktopcu.customalarmapp.presentation.navigation.Screen

@Composable
fun AlarmListScreen(navController: NavController) {
    val context = LocalContext.current
    val alarmManager = remember { AlarmManager(context) }
    val alarmScheduler = remember { AlarmScheduler(context) }
    val alarms = remember { mutableStateListOf<Alarm>().apply { addAll(alarmManager.getAllAlarms()) } }

    Scaffold(
        timeText = { TimeText() }
    ) {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {


            // Add Alarm Button
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colors.primary)
                            .clickable { navController.navigate(Screen.AlarmEdit.route) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Alarm Ekle",
                            tint = MaterialTheme.colors.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Alarm List Items
            items(alarms, key = { it.id }) { alarm ->
                Card(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .requiredHeight(70.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${alarm.hour.toString().padStart(2, '0')}:${alarm.minute.toString().padStart(2, '0')}",
                                style = MaterialTheme.typography.body1,
                                color = Color.White
                            )
                            Text(
                                text = alarm.label,
                                style = MaterialTheme.typography.body2.copy(fontSize = 36.sp),                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Switch(
                            checked = alarm.isEnabled,
                            onCheckedChange = { newValue ->
                                alarmManager.toggleAlarm(alarm.id)
                                if (newValue) {
                                    alarmScheduler.scheduleAlarm(alarm.id, alarm.timeInMillis, alarm.label)
                                } else {
                                    alarmScheduler.cancelAlarm(alarm.id)
                                }
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