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
import com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmScheduler
import com.turkerberktopcu.customalarmapp.presentation.alarm.VibrationPattern
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun AlarmEditScreen(navController: NavController) {
    val context = LocalContext.current
    val alarmManager = remember { com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmManager(context) }
    val alarmScheduler = remember { com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmScheduler(context) }
    var showLabelInput by remember { mutableStateOf(false) }

    val hours = (0..23).toList()
    val minutes = (0..59).toList()

    val hourPickerState = rememberPickerState(hours.size, initiallySelectedOption = 6)
    val minutePickerState = rememberPickerState(minutes.size, initiallySelectedOption = 0)

    var selectedHour by remember { mutableStateOf(hours[hourPickerState.selectedOption]) }
    var selectedMinute by remember { mutableStateOf(minutes[minutePickerState.selectedOption]) }
    var alarmLabel by remember { mutableStateOf("") }
    var dailyResetEnabled by remember { mutableStateOf(false) }

    val savedStateHandle = navController.currentBackStackEntry
        ?.savedStateHandle

    val selectedVibration by savedStateHandle?.getStateFlow<VibrationPattern?>(
        "selectedVibration",
        VibrationPattern.None // Set default to None
    )?.collectAsState() ?: remember { mutableStateOf(VibrationPattern.None) }


    LaunchedEffect(hourPickerState.selectedOption) {
        selectedHour = hours[hourPickerState.selectedOption]
    }
    LaunchedEffect(minutePickerState.selectedOption) {
        selectedMinute = minutes[minutePickerState.selectedOption]
    }
    val snoozeCounts = listOf(0) + (1..10).toList() // First item is 0 (unlimited)
    val snoozePickerState = rememberPickerState(snoozeCounts.size)
    var selectedSnoozeCount by remember {
        mutableStateOf(snoozeCounts[snoozePickerState.selectedOption])
    }

    LaunchedEffect(snoozePickerState.selectedOption) {
        selectedSnoozeCount = snoozeCounts[snoozePickerState.selectedOption]
    }

    Scaffold(
        timeText = { TimeText() }
    ) {
        // Add a ScalingLazyColumn for scrollability
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
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
                        text = "Saat Ayarla",
                        style = MaterialTheme.typography.title2,
                        color = Color.White
                    )
                }
            }

            // Time Pickers
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

            // Daily Reset Checkbox - Make it more visible
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
                            onCheckedChange = { enabled ->
                                dailyResetEnabled = enabled
                            }
                        )
                    }
                }
            }

            // Add this item to the ScalingLazyColumn
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
                            text = selectedVibration?.let {
                                it.getDisplayName()
                            } ?: "Vibration Mode",
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
            // Custom Input Field
            item {
                var showDialog by remember { mutableStateOf(false) }

                // Card that acts as an input field
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

                // Custom Dialog for entering the alarm label
                if (showDialog) {
                    Dialog(onDismissRequest = { showDialog = false }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.Gray, shape = RoundedCornerShape(12.dp))
                                .background(Color.Black, shape = RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Alarm Etiketi Girin",
                                    color = Color.White,
                                    style = MaterialTheme.typography.title2
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Basic TextField with Wear OS friendly styling
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

                                // Button Row (Cancel & Confirm)
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

            // Action Button
            item {
                Button(
                    onClick = {
                        handleAlarmCreation(
                            context = context,
                            hour = selectedHour,
                            minute = selectedMinute,
                            label = alarmLabel,
                            dailyReset = dailyResetEnabled,
                            navController = navController,
                            maxSnooze = selectedSnoozeCount,
                            vibrationPattern = selectedVibration
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
private fun handleAlarmCreation(
    context: Context,
    hour: Int,
    minute: Int,
    label: String,
    dailyReset: Boolean,
    maxSnooze: Int,  // Add parameter
    navController: NavController,
    vibrationPattern: VibrationPattern?

)  {
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

    val newAlarm = alarmManager.addAlarm(hour, minute, label, dailyReset, maxSnooze, vibrationPattern)
    alarmScheduler.scheduleAlarm(newAlarm.id, newAlarm.timeInMillis, newAlarm.label)
    navController.popBackStack()
}
