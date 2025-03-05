package com.turkerberktopcu.customalarmapp.presentation

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.wear.compose.material.*
import com.turkerberktopcu.customalarmapp.presentation.alarm.Alarm
import com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmScheduler
import com.turkerberktopcu.customalarmapp.presentation.alarm.VibrationPattern
import java.util.Calendar
// In AlarmEditScreen.kt

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun AlarmEditScreen(navController: NavController) {
    val context = LocalContext.current
    val alarmManager = remember { com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmManager(context) }
    val alarmScheduler = remember { com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmScheduler(context) }
    var showLabelInput by remember { mutableStateOf(false) }

    // Retrieve alarmId from navigation arguments
    val alarmId = navController.currentBackStackEntry?.arguments?.getString("alarmId")?.toIntOrNull()
    val alarm = alarmId?.let { id -> alarmManager.alarms.find { it.id == id } }

    val hours = (0..23).toList()
    val minutes = (0..59).toList()
    val snoozeCounts = listOf(0) + (1..10).toList()

    // Initialize states with alarm data if available
    val hourPickerState = rememberPickerState(
        hours.size,
        initiallySelectedOption = alarm?.hour ?: 6
    )
    val minutePickerState = rememberPickerState(
        minutes.size,
        initiallySelectedOption = alarm?.minute ?: 0
    )
    val snoozePickerState = rememberPickerState(
        snoozeCounts.size,
        initiallySelectedOption = alarm?.maxSnoozeCount ?: 0
    )

    var selectedHour by remember { mutableStateOf(alarm?.hour ?: hours[hourPickerState.selectedOption]) }
    var selectedMinute by remember { mutableStateOf(alarm?.minute ?: minutes[minutePickerState.selectedOption]) }
    var alarmLabel by remember { mutableStateOf(alarm?.label ?: "") }
    var dailyResetEnabled by remember { mutableStateOf(alarm?.isDailyReset ?: false) }
    var selectedSnoozeCount by remember { mutableStateOf(alarm?.maxSnoozeCount ?: snoozeCounts[snoozePickerState.selectedOption]) }
    val snoozeIntervalOptions = (1..60).toList()
// Initialize picker state; if editing an existing alarm, convert from millis to seconds
    val snoozeIntervalPickerState = rememberPickerState(
        snoozeIntervalOptions.size,
        initiallySelectedOption = alarm?.snoozeIntervalMillis?.div(60000)?.toInt() ?: 0
    )
    var selectedSnoozeIntervalSeconds by remember {
        mutableStateOf(snoozeIntervalOptions[snoozeIntervalPickerState.selectedOption])
    }

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val selectedVibration by savedStateHandle?.getStateFlow<VibrationPattern?>(
        "selectedVibration",
        alarm?.vibrationPattern ?: VibrationPattern.None // Use alarm's vibration if editing
    )?.collectAsState() ?: remember { mutableStateOf(alarm?.vibrationPattern ?: VibrationPattern.None) }

    // Set initial vibration pattern for existing alarm
    LaunchedEffect(alarm) {
        if (alarm != null) {
            savedStateHandle?.set("selectedVibration", alarm.vibrationPattern)
        }
    }
// Update the state when the picker selection changes
    LaunchedEffect(snoozeIntervalPickerState.selectedOption) {
        selectedSnoozeIntervalSeconds = snoozeIntervalOptions[snoozeIntervalPickerState.selectedOption]
    }
    LaunchedEffect(hourPickerState.selectedOption) {
        selectedHour = hours[hourPickerState.selectedOption]
    }
    LaunchedEffect(minutePickerState.selectedOption) {
        selectedMinute = minutes[minutePickerState.selectedOption]
    }
    LaunchedEffect(snoozePickerState.selectedOption) {
        selectedSnoozeCount = snoozeCounts[snoozePickerState.selectedOption]
    }

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
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (alarm != null) "Alarm Düzenle" else "Saat Ayarla", // Dynamic title
                        style = MaterialTheme.typography.title2,
                        color = Color.White
                    )
                }
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    TimePickerSection(
                        state = hourPickerState,
                        items = hours,
                        modifier = Modifier.width(70.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TimePickerSection(
                        state = minutePickerState,
                        items = minutes,
                        modifier = Modifier.width(70.dp)
                    )
                }
            }

            item {
                Card(
                    onClick = { dailyResetEnabled = !dailyResetEnabled },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Günlük Tekrar",
                            color = Color.White,
                            style = MaterialTheme.typography.body1
                        )
                        Checkbox(
                            checked = dailyResetEnabled,
                            onCheckedChange = { enabled -> dailyResetEnabled = enabled }
                        )
                    }
                }
            }

            item {
                Card(
                    onClick = { navController.navigate("vibrationSelection") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = selectedVibration?.let { it.getDisplayName() } ?: "Vibration Mode",
                            color = Color.White
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Select Vibration"
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Erteleme Say:", color = Color.White)
                    Picker(
                        state = snoozePickerState,
                        modifier = Modifier.size(100.dp, 60.dp)
                    ) {
                        Text(
                            text = when (snoozeCounts[it]) {
                                0 -> "Sınırsız"
                                else -> snoozeCounts[it].toString()
                            },
                            style = MaterialTheme.typography.body1
                        )
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Erteleme Aralığı (sn):", color = Color.White)
                    Picker(
                        state = snoozeIntervalPickerState,
                        modifier = Modifier.size(100.dp, 60.dp)
                    ) {
                        Text(
                            text = "${snoozeIntervalOptions[it]}",
                            style = MaterialTheme.typography.body1
                        )
                    }
                }
            }

            item {
                var showDialog by remember { mutableStateOf(false) }
                Card(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = alarmLabel.ifEmpty { "Alarm Etiketi" },
                            color = if (alarmLabel.isEmpty()) Color.Gray else Color.White,
                            style = MaterialTheme.typography.body1
                        )
                    }
                }
                if (showDialog) {
                    Dialog(onDismissRequest = { showDialog = false }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.Gray, shape = RoundedCornerShape(12.dp))
                                .background(Color.Black, shape = RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Alarm Etiketi Girin",
                                    color = Color.White,
                                    style = MaterialTheme.typography.title2
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color.White, shape = RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    BasicTextField(
                                        value = alarmLabel,
                                        onValueChange = { alarmLabel = it },
                                        singleLine = true,
                                        textStyle = TextStyle(color = Color.White),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { showDialog = false }) {
                                        Text("İptal", color = Color.Gray)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    TextButton(onClick = { showDialog = false }) {
                                        Text("Tamam", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        handleAlarmSave(
                            context = context,
                            hour = selectedHour,
                            minute = selectedMinute,
                            label = alarmLabel,
                            dailyReset = dailyResetEnabled,
                            maxSnooze = selectedSnoozeCount,
                            vibrationPattern = selectedVibration,
                            alarm = alarm, // Pass the loaded alarm
                            navController = navController,
                            selectedSnoozeIntervalSeconds = selectedSnoozeIntervalSeconds
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text("Kaydet", color = Color.White)
                }
            }
        }
    }
}

// Extension function for display names
fun VibrationPattern.getDisplayName(): String {
    return when (this) {
        VibrationPattern.Default -> "Default"
        VibrationPattern.Short -> "Short"
        VibrationPattern.Long -> "Long"
        VibrationPattern.Custom -> "Custom"
        VibrationPattern.None -> "No Vibration"
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
private fun handleAlarmSave(
    context: Context,
    hour: Int,
    minute: Int,
    label: String,
    dailyReset: Boolean,
    maxSnooze: Int,
    vibrationPattern: VibrationPattern?,
    alarm: Alarm?, // Determines if editing or adding
    navController: NavController,
    selectedSnoozeIntervalSeconds: Int  // New parameter
) {
    val alarmManager = com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmManager(context)
    val alarmScheduler = com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmScheduler(context)
    val systemAlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    if (!systemAlarmManager.canScheduleExactAlarms()) {
        context.startActivity(
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        return
    }

    if (alarm != null) {
        // Update existing alarm
        val updatedAlarm = alarm.copy(
            hour = hour,
            minute = minute,
            label = label,
            isDailyReset = dailyReset,
            maxSnoozeCount = maxSnooze,
            vibrationPattern = vibrationPattern ?: VibrationPattern.None,
            timeInMillis = alarmManager.calculateTriggerTime(hour, minute),
            snoozeIntervalMillis = selectedSnoozeIntervalSeconds * 60_000L
        )
        alarmManager.updateAlarm(updatedAlarm)
        if (updatedAlarm.isEnabled) {
            alarmScheduler.cancelAlarm(updatedAlarm.id)
            alarmScheduler.scheduleAlarm(updatedAlarm.id, updatedAlarm.timeInMillis, updatedAlarm.label)
        }
    } else {
        // Add new alarm
        val newAlarm = alarmManager.addAlarm(
            hour,
            minute,
            label,
            dailyReset,
            maxSnooze,
            vibrationPattern,
            selectedSnoozeIntervalSeconds * 60_000L
        )
        alarmScheduler.scheduleAlarm(newAlarm.id, newAlarm.timeInMillis, newAlarm.label)
    }
    navController.popBackStack()
}
