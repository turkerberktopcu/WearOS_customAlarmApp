package com.turkerberktopcu.customalarmapp.presentation

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
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

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun AlarmEditScreen(navController: NavController) {
    val context = LocalContext.current
    val alarmManager = remember { com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmManager(context) }
    val alarmScheduler = remember { com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmScheduler(context) }

    // Retrieve alarmId from navigation arguments
    val alarmId = navController.currentBackStackEntry
        ?.arguments
        ?.getString("alarmId")
        ?.toIntOrNull()
    val alarm = alarmId?.let { id -> alarmManager.alarms.find { it.id == id } }

    // Hour/minute pickers
    val hours = (0..23).toList()
    val minutes = (0..59).toList()
    val hourPickerState = rememberPickerState(
        initialNumberOfOptions = hours.size,
        initiallySelectedOption = alarm?.hour ?: 6
    )
    val minutePickerState = rememberPickerState(
        initialNumberOfOptions = minutes.size,
        initiallySelectedOption = alarm?.minute ?: 0
    )
    var selectedHour by remember { mutableStateOf(alarm?.hour ?: hours[hourPickerState.selectedOption]) }
    var selectedMinute by remember { mutableStateOf(alarm?.minute ?: minutes[minutePickerState.selectedOption]) }

    // Label
    var alarmLabel by remember { mutableStateOf(alarm?.label ?: "") }
    var showLabelDialog by remember { mutableStateOf(false) }

    // Daily reset
    var dailyResetEnabled by remember { mutableStateOf(alarm?.isDailyReset ?: false) }

    // Snooze count
    val snoozeCounts = listOf(0) + (1..10).toList()
    val snoozePickerState = rememberPickerState(
        initialNumberOfOptions = snoozeCounts.size,
        initiallySelectedOption = alarm?.maxSnoozeCount ?: 0
    )
    var selectedSnoozeCount by remember {
        mutableStateOf(alarm?.maxSnoozeCount ?: snoozeCounts[snoozePickerState.selectedOption])
    }

    // Snooze interval
    val snoozeIntervalOptions = (1..60).toList()
    val snoozeIntervalPickerState = rememberPickerState(
        initialNumberOfOptions = snoozeIntervalOptions.size,
        initiallySelectedOption = ((alarm?.snoozeIntervalMillis?.div(60000)?.toInt() ?: 1) - 1)
    )
    var selectedSnoozeIntervalMinutes by remember {
        mutableStateOf(snoozeIntervalOptions[snoozeIntervalPickerState.selectedOption])
    }

    // Working duration
    val workingDurationOptions = (1..60).toList()
    val workingDurationPickerState = rememberPickerState(
        initialNumberOfOptions = workingDurationOptions.size,
        initiallySelectedOption = ((alarm?.workingDurationMillis?.div(60000)?.toInt() ?: 5) - 1)
    )
    var selectedWorkingDurationMinutes by remember {
        mutableStateOf(workingDurationOptions[workingDurationPickerState.selectedOption])
    }

    // Break duration
    val breakDurationOptions = (1..60).toList()
    val breakDurationPickerState = rememberPickerState(
        initialNumberOfOptions = breakDurationOptions.size,
        initiallySelectedOption = ((alarm?.breakDurationMillis?.div(60000)?.toInt() ?: 2) - 1)
    )
    var selectedBreakDurationMinutes by remember {
        mutableStateOf(breakDurationOptions[breakDurationPickerState.selectedOption])
    }

    // Listen for changes in pickers
    LaunchedEffect(hourPickerState.selectedOption) {
        selectedHour = hours[hourPickerState.selectedOption]
    }
    LaunchedEffect(minutePickerState.selectedOption) {
        selectedMinute = minutes[minutePickerState.selectedOption]
    }
    LaunchedEffect(snoozePickerState.selectedOption) {
        selectedSnoozeCount = snoozeCounts[snoozePickerState.selectedOption]
    }
    LaunchedEffect(snoozeIntervalPickerState.selectedOption) {
        selectedSnoozeIntervalMinutes = snoozeIntervalOptions[snoozeIntervalPickerState.selectedOption]
    }
    LaunchedEffect(workingDurationPickerState.selectedOption) {
        selectedWorkingDurationMinutes = workingDurationOptions[workingDurationPickerState.selectedOption]
    }
    LaunchedEffect(breakDurationPickerState.selectedOption) {
        selectedBreakDurationMinutes = breakDurationOptions[breakDurationPickerState.selectedOption]
    }

    // Vibration pattern from a separate selection screen
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val selectedVibration by savedStateHandle?.getStateFlow<VibrationPattern?>(
        "selectedVibration",
        alarm?.vibrationPattern ?: VibrationPattern.None
    )?.collectAsState() ?: remember {
        mutableStateOf(alarm?.vibrationPattern ?: VibrationPattern.None)
    }
    LaunchedEffect(alarm) {
        if (alarm != null) {
            savedStateHandle?.set("selectedVibration", alarm.vibrationPattern)
        }
    }

    // --------------- SOUND SELECTION ---------------
    // We'll store the chosen alarm sound URI in local state
    var selectedAlarmSound by remember {
        mutableStateOf(
            alarm?.alarmSoundUri
                ?: "" // default if no alarm
        )
    }
    // Watch for "selectedAlarmSound" from the "sound selection" screen
    LaunchedEffect(savedStateHandle?.get<String>("selectedAlarmSound")) {
        val chosenSound = savedStateHandle?.get<String>("selectedAlarmSound")
        if (chosenSound != null) {
            selectedAlarmSound = chosenSound
            // Optionally remove the key after using it
            savedStateHandle.remove<String>("selectedAlarmSound")
        }
    }

    // Wear Scaffold
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
            // Title row
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
                        text = if (alarm != null) "Alarm Düzenle" else "Saat Ayarla",
                        style = MaterialTheme.typography.title2,
                        color = Color.White
                    )
                }
            }

            // Working duration
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Çalışma Süresi(dk):", color = Color.White)
                    Picker(
                        state = workingDurationPickerState,
                        modifier = Modifier.size(100.dp, 60.dp)
                    ) { index ->
                        Text(
                            text = "${workingDurationOptions[index]}",
                            style = MaterialTheme.typography.body1
                        )
                    }
                }
            }

            // Break duration
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Çalışma Arası(dk):", color = Color.White)
                    Picker(
                        state = breakDurationPickerState,
                        modifier = Modifier.size(100.dp, 60.dp)
                    ) { index ->
                        Text(
                            text = "${breakDurationOptions[index]}",
                            style = MaterialTheme.typography.body1
                        )
                    }
                }
            }

            // Hour/minute pickers side by side
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

            // Daily Reset
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
                        Text(
                            text = if (dailyResetEnabled) "Açık" else "Kapalı",
                            color = Color.White,
                            style = MaterialTheme.typography.body1
                        )
                    }
                }
            }

            // Vibration selection
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
                            text = selectedVibration?.getDisplayName() ?: "Vibration Mode",
                            color = Color.White
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Select Vibration"
                        )
                    }
                }
            }

            // Snooze count
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
                    ) { index ->
                        val countValue = snoozeCounts[index]
                        Text(
                            text = if (countValue == 0) "Sınırsız" else countValue.toString(),
                            style = MaterialTheme.typography.body1
                        )
                    }
                }
            }

            // Snooze interval
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Erteleme Süresi(dk):", color = Color.White)
                    Picker(
                        state = snoozeIntervalPickerState,
                        modifier = Modifier.size(100.dp, 60.dp)
                    ) { index ->
                        Text(
                            text = "${snoozeIntervalOptions[index]}",
                            style = MaterialTheme.typography.body1
                        )
                    }
                }
            }

            // Alarm sound selection
            item {
                Card(
                    onClick = { navController.navigate("alarmSoundSelection") },
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
                        // Simple approach: if "NO_SOUND", show "No Sound"; else "Custom Sound"
                        // Or you can parse the URI to get a title
                        val soundLabel = if (selectedAlarmSound == "NO_SOUND") {
                            "No Sound"
                        } else if (selectedAlarmSound.isNullOrEmpty()) {
                            "Select Sound"
                        } else {
                            "Custom Sound"
                        }

                        Text(
                            text = soundLabel,
                            color = Color.White
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Select Alarm Sound"
                        )
                    }
                }
            }

            // Alarm label (dialog)
            item {
                Card(
                    onClick = { showLabelDialog = true },
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
                if (showLabelDialog) {
                    Dialog(onDismissRequest = { showLabelDialog = false }) {
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
                                    TextButton(onClick = { showLabelDialog = false }) {
                                        Text("İptal", color = Color.Gray)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    TextButton(onClick = { showLabelDialog = false }) {
                                        Text("Tamam", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Save button
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
                            alarm = alarm,
                            navController = navController,
                            selectedSnoozeIntervalMinutes = selectedSnoozeIntervalMinutes,
                            selectedWorkingDurationMinutes = selectedWorkingDurationMinutes,
                            selectedBreakDurationMinutes = selectedBreakDurationMinutes,
                            alarmSoundUri = selectedAlarmSound // pass the chosen sound here
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

// Extension function for display names of VibrationPattern
fun VibrationPattern.getDisplayName(): String {
    return when (this) {
        VibrationPattern.Default -> "Default"
        VibrationPattern.Short -> "Short"
        VibrationPattern.Long -> "Long"
        VibrationPattern.Custom -> "Custom"
        VibrationPattern.None -> "No Vibration"
    }
}

/**
 * Helper function to finalize or update the alarm.
 */
@RequiresApi(Build.VERSION_CODES.S)
private fun handleAlarmSave(
    context: Context,
    hour: Int,
    minute: Int,
    label: String,
    dailyReset: Boolean,
    maxSnooze: Int,
    vibrationPattern: VibrationPattern?,
    alarm: Alarm?, // Editing or creating a new alarm
    navController: NavController,
    selectedSnoozeIntervalMinutes: Int,
    selectedWorkingDurationMinutes: Int,
    selectedBreakDurationMinutes: Int,
    alarmSoundUri: String? // new param for chosen sound
) {
    val alarmManager = com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmManager(context)
    val alarmScheduler = com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmScheduler(context)
    val systemAlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // If the user hasn't granted exact alarm permission (API 31+)
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
            snoozeIntervalMillis = selectedSnoozeIntervalMinutes * 60_000L,
            workingDurationMillis = selectedWorkingDurationMinutes * 60_000L,
            breakDurationMillis = selectedBreakDurationMinutes * 60_000L,
            alarmSoundUri = alarmSoundUri
        )
        alarmManager.updateAlarm(updatedAlarm)

        // Reschedule if still enabled
        if (updatedAlarm.isEnabled) {
            alarmScheduler.cancelAlarm(updatedAlarm.id)
            alarmScheduler.scheduleAlarm(updatedAlarm.id, updatedAlarm.timeInMillis, updatedAlarm.label)
        }
    } else {
        // Create new alarm
        val newAlarm = alarmManager.addAlarm(
            hour,
            minute,
            label,
            dailyReset,
            maxSnooze,
            vibrationPattern,
            selectedSnoozeIntervalMinutes * 60_000L,
            selectedWorkingDurationMinutes * 60_000L,
            selectedBreakDurationMinutes * 60_000L,
            alarmSoundUri // pass here
        )
        alarmScheduler.scheduleAlarm(newAlarm.id, newAlarm.timeInMillis, newAlarm.label)
    }

    // Go back after saving
    navController.popBackStack()
}
