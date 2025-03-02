package com.turkerberktopcu.customalarmapp.presentation

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.material.*
import com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmScheduler
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun AlarmEditScreen(navController: NavController) {
    val context = LocalContext.current
    val alarmManager = remember { com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmManager(context) }
    val alarmScheduler = remember { com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmScheduler(context) }

    val hours = (0..23).toList()
    val minutes = (0..59).toList()

    val hourPickerState = rememberPickerState(hours.size, initiallySelectedOption = 6)
    val minutePickerState = rememberPickerState(minutes.size, initiallySelectedOption = 0)

    var selectedHour by remember { mutableStateOf(hours[hourPickerState.selectedOption]) }
    var selectedMinute by remember { mutableStateOf(minutes[minutePickerState.selectedOption]) }
    var alarmLabel by remember { mutableStateOf("") }
    var dailyResetEnabled by remember { mutableStateOf(false) } // Local state

    LaunchedEffect(hourPickerState.selectedOption) {
        selectedHour = hours[hourPickerState.selectedOption]
    }
    LaunchedEffect(minutePickerState.selectedOption) {
        selectedMinute = minutes[minutePickerState.selectedOption]
    }

    Scaffold(
        timeText = { TimeText() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Geri",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Saat Ayarla",
                    style = MaterialTheme.typography.title2,
                    color = Color.White
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Time Pickers
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    PickerLabel("")
                    TimePickerSection(
                        state = hourPickerState,
                        items = hours,
                        modifier = Modifier.width(80.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    PickerLabel("")
                    TimePickerSection(
                        state = minutePickerState,
                        items = minutes,
                        modifier = Modifier.width(80.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Daily Reset Checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Reset",
                        modifier = Modifier.weight(1f),
                        color = Color.White,
                        style = MaterialTheme.typography.body1
                    )
                    Checkbox(
                        checked = dailyResetEnabled,
                        onCheckedChange = { enabled ->
                            dailyResetEnabled = enabled
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Input Field
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .border(1.dp, Color.White, MaterialTheme.shapes.small)
                        .padding(12.dp)
                ) {
                    Text(
                        text = alarmLabel.ifEmpty { "Alarm Etiketi" },
                        color = if (alarmLabel.isEmpty()) Color.Gray else Color.White,
                        style = MaterialTheme.typography.body1
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Button
            Button(onClick = {
                handleAlarmCreation(
                    context = context,
                    hour = selectedHour,
                    minute = selectedMinute,
                    label = alarmLabel,
                    dailyReset = dailyResetEnabled,
                    navController = navController
                )
            }) {
                Text("Kaydet", color = Color.White)
            }
        }
    }
}


@Composable
private fun TimePickerSection(
    state: PickerState,
    items: List<Int>,
    modifier: Modifier = Modifier
) {
    Picker(
        state = state,
        modifier = modifier.height(120.dp)
    ) { index ->
        Text(
            text = items[index].toString().padStart(2, '0'),
            color = Color.White,
            style = MaterialTheme.typography.display2
        )
    }
}

@Composable
private fun PickerLabel(text: String) {
    Text(
        text = text,
        color = Color.White,
        style = MaterialTheme.typography.body2,
        modifier = Modifier.padding(end = 8.dp)
    )
}
@RequiresApi(Build.VERSION_CODES.S)
private fun handleAlarmCreation(
    context: Context,
    hour: Int,
    minute: Int,
    label: String,
    dailyReset: Boolean, // Add this parameter
    navController: NavController
) {
    val alarmManager = com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmManager(context)
    val alarmScheduler = com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmScheduler(context)

    // Rest of your logic (time calculation, scheduling, etc.)
    val systemAlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    if (!systemAlarmManager.canScheduleExactAlarms()) {
        context.startActivity(
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        return
    }
    val testTriggerTime = System.currentTimeMillis() + 60000 // 15 seconds from now
    Log.d("Test", "Test time: ${testTriggerTime}")

    val newAlarm = alarmManager.addAlarm(hour, minute, label, dailyReset)
    alarmScheduler.scheduleAlarm(newAlarm.id, newAlarm.timeInMillis, newAlarm.label)
    navController.popBackStack()
}