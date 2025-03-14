package com.turkerberktopcu.customalarmapp.presentation

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.wear.compose.material.*
import com.turkerberktopcu.customalarmapp.presentation.alarm.Alarm
import com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmScheduler
import com.turkerberktopcu.customalarmapp.presentation.alarm.VibrationPattern

@Composable
fun AlarmEditScreen(navController: NavController) {
    val context = LocalContext.current
    val systemAlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val alarmManager = remember { com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmManager(context) }
    val alarmScheduler = remember { AlarmScheduler(context) }

    // Retrieve alarmId from navigation arguments
    val alarmId = navController.currentBackStackEntry
        ?.arguments
        ?.getString("alarmId")
        ?.toIntOrNull()
    val alarm = alarmId?.let { id -> alarmManager.alarms.find { it.id == id } }

    // Tanımlı saat ve dakika listeleri
    val hours = (0..23).toList()
    val minutes = (0..59).toList()

    // Picker state'leri
    val hourPickerState = rememberPickerState(initialNumberOfOptions = hours.size, initiallySelectedOption = alarm?.hour ?: 6)
    val minutePickerState = rememberPickerState(initialNumberOfOptions = minutes.size, initiallySelectedOption = alarm?.minute ?: 0)

    // Derived state ile seçili saat ve dakika
    val selectedHour by remember { derivedStateOf { hours[hourPickerState.selectedOption] } }
    val selectedMinute by remember { derivedStateOf { minutes[minutePickerState.selectedOption] } }

    // Alarm Label
    var alarmLabel by remember { mutableStateOf(alarm?.label ?: "") }
    var showLabelDialog by remember { mutableStateOf(false) }

    // Daily reset
    var dailyResetEnabled by remember { mutableStateOf(alarm?.isDailyReset ?: false) }

    // Snooze count, interval, working & break duration listeleri ve picker state'leri
    val snoozeCounts = listOf(0) + (1..10).toList()
    val snoozePickerState = rememberPickerState(initialNumberOfOptions = snoozeCounts.size, initiallySelectedOption = alarm?.maxSnoozeCount ?: 0)
    val selectedSnoozeCount by remember { derivedStateOf { snoozeCounts[snoozePickerState.selectedOption] } }

    val snoozeIntervalOptions = (1..60).toList()
    val snoozeIntervalPickerState = rememberPickerState(
        initialNumberOfOptions = snoozeIntervalOptions.size,
        initiallySelectedOption = ((alarm?.snoozeIntervalMillis?.div(60000)?.toInt() ?: 1) - 1).coerceAtLeast(0)
    )
    val selectedSnoozeIntervalMinutes by remember { derivedStateOf { snoozeIntervalOptions[snoozeIntervalPickerState.selectedOption] } }

    val workingDurationOptions = (1..60).toList()
    val workingDurationPickerState = rememberPickerState(
        initialNumberOfOptions = workingDurationOptions.size,
        initiallySelectedOption = ((alarm?.workingDurationMillis?.div(60000)?.toInt() ?: 5) - 1).coerceAtLeast(0)
    )
    val selectedWorkingDurationMinutes by remember { derivedStateOf { workingDurationOptions[workingDurationPickerState.selectedOption] } }

    val breakDurationOptions = (1..60).toList()
    val breakDurationPickerState = rememberPickerState(
        initialNumberOfOptions = breakDurationOptions.size,
        initiallySelectedOption = ((alarm?.breakDurationMillis?.div(60000)?.toInt() ?: 2) - 1).coerceAtLeast(0)
    )
    val selectedBreakDurationMinutes by remember { derivedStateOf { breakDurationOptions[breakDurationPickerState.selectedOption] } }

    // Vibration pattern: saved state üzerinden güncelleme
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val selectedVibration by savedStateHandle?.getStateFlow<VibrationPattern?>(
        "selectedVibration",
        alarm?.vibrationPattern ?: VibrationPattern.None
    )?.collectAsState() ?: remember { mutableStateOf(alarm?.vibrationPattern ?: VibrationPattern.None) }
    LaunchedEffect(alarm) {
        alarm?.let { savedStateHandle?.set("selectedVibration", it.vibrationPattern) }
    }

    // Alarm sound selection: yerel state
    var selectedAlarmSound by remember { mutableStateOf(alarm?.alarmSoundUri ?: "") }
    LaunchedEffect(savedStateHandle?.get<String>("selectedAlarmSound")) {
        savedStateHandle?.get<String>("selectedAlarmSound")?.let {
            selectedAlarmSound = it
            savedStateHandle.remove<String>("selectedAlarmSound")
        }
    }

    // Scaffold (WearOS UI)
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
            // Warning message if permission isn't granted
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !systemAlarmManager.canScheduleExactAlarms()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Uyarı: Tam alarm izni verilmedi. Alarmlar gecikmeli çalabilir.",
                            color = Color.Yellow,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        TextButton(onClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }) {
                            Text("İzin Ver", color = Color.Yellow)
                        }
                    }
                }
            }

            // Title row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.size(40.dp)) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (alarm != null) "Alarm Düzenle" else "Saat Ayarla",
                        style = MaterialTheme.typography.title2.copy(fontSize = 16.sp),
                        color = Color.White
                    )
                }
            }

            // Working Duration Picker
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

            // Break Duration Picker
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

            // Saat/Dakika Picker'ları yan yana göster
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    TimePickerSection(state = hourPickerState, items = hours, modifier = Modifier.width(70.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    TimePickerSection(state = minutePickerState, items = minutes, modifier = Modifier.width(70.dp))
                }
            }

            // Daily Reset Card
            item {
                Card(
                    onClick = { dailyResetEnabled = !dailyResetEnabled },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Günlük Tekrar", color = Color.White, style = MaterialTheme.typography.body1)
                        Text(if (dailyResetEnabled) "Açık" else "Kapalı", color = Color.White, style = MaterialTheme.typography.body1)
                    }
                }
            }

            // Vibration Selection Card
            item {
                Card(
                    onClick = { navController.navigate("vibrationSelection") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(selectedVibration?.getDisplayName() ?: "Vibration Mode", color = Color.White)
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Select Vibration")
                    }
                }
            }

            // Snooze Count Picker
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
                        Text(text = if (countValue == 0) "Sınırsız" else countValue.toString(), style = MaterialTheme.typography.body1)
                    }
                }
            }

            // Snooze Interval Picker
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
                        Text(text = "${snoozeIntervalOptions[index]}", style = MaterialTheme.typography.body1)
                    }
                }
            }

            // Alarm Sound Selection Card
            item {
                Card(
                    onClick = { navController.navigate("alarmSoundSelection") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val soundLabel = when {
                            selectedAlarmSound == "NO_SOUND" -> "No Sound"
                            selectedAlarmSound.isEmpty() -> "Select Sound"
                            else -> "Custom Sound"
                        }
                        Text(text = soundLabel, color = Color.White)
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Select Alarm Sound")
                    }
                }
            }

            // Alarm Label Card & Dialog
            item {
                Card(
                    onClick = { showLabelDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
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
                                Text("Alarm Etiketi Girin", color = Color.White, style = MaterialTheme.typography.title2)
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

            // Save Button
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
                            alarmSoundUri = selectedAlarmSound
                        )
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text("Kaydet", color = Color.White)
                }
            }
        }
    }
}

private fun handleAlarmSave(
    context: Context,
    hour: Int,
    minute: Int,
    label: String,
    dailyReset: Boolean,
    maxSnooze: Int,
    vibrationPattern: VibrationPattern?,
    alarm: Alarm?,
    navController: NavController,
    selectedSnoozeIntervalMinutes: Int,
    selectedWorkingDurationMinutes: Int,
    selectedBreakDurationMinutes: Int,
    alarmSoundUri: String?
) {
    val alarmManager = com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmManager(context)
    val alarmScheduler = com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmScheduler(context)
    val systemAlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // API 31 ve üzeri cihazlarda exact alarm izni kontrolü yapıyoruz.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !systemAlarmManager.canScheduleExactAlarms()) {
        Log.w("handleAlarmSave", "Exact alarm izni verilmedi. İzin ekranına yönlendiriliyor.")
        Toast.makeText(
            context,
            "Alarm gecikmeli çalabilir, tam alarm izni verilmedi.",
            Toast.LENGTH_LONG
        ).show()
    }

    try {
        if (alarm != null) {
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
            if (updatedAlarm.isEnabled) {
                alarmScheduler.cancelAlarm(updatedAlarm.id)
                alarmScheduler.scheduleAlarm(updatedAlarm.id, updatedAlarm.timeInMillis, updatedAlarm.label)
            }
        } else {
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
                alarmSoundUri
            )
            alarmScheduler.scheduleAlarm(newAlarm.id, newAlarm.timeInMillis, newAlarm.label)
        }
    } catch (e: Exception) {
        Log.e("handleAlarmSave", "Alarm kaydedilirken hata: ${e.localizedMessage}", e)
    }

    navController.popBackStack()
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
            style = MaterialTheme.typography.display2.copy(fontSize = 24.sp)
        )
    }
}

// Extension for VibrationPattern display name
fun VibrationPattern.getDisplayName(): String {
    return when (this) {
        VibrationPattern.Default -> "Default"
        VibrationPattern.Short -> "Short"
        VibrationPattern.Long -> "Long"
        VibrationPattern.Custom -> "Custom"
        VibrationPattern.None -> "No Vibration"
    }
}