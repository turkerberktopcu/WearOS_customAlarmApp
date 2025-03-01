package com.turkerberktopcu.customalarmapp.presentation

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.material.Picker
import androidx.wear.compose.material.rememberPickerState
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.wear.compose.material.MaterialTheme

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun AlarmEditScreen(navController: NavController) {
    val context = LocalContext.current

    val hours = (0..23).toList()
    val minutes = (0..59).toList()

    val hourPickerState = rememberPickerState(hours.size, initiallySelectedOption = 6)
    val minutePickerState = rememberPickerState(minutes.size, initiallySelectedOption = 0)

    var selectedHourIndex by remember { mutableStateOf(hourPickerState.selectedOption) }
    var selectedMinuteIndex by remember { mutableStateOf(minutePickerState.selectedOption) }

    LaunchedEffect(hourPickerState.selectedOption) {
        selectedHourIndex = hourPickerState.selectedOption
    }
    LaunchedEffect(minutePickerState.selectedOption) {
        selectedMinuteIndex = minutePickerState.selectedOption
    }

    var alarmLabel by remember { mutableStateOf("") }
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
            // Geri butonu
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Geri",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Saat ayarla",
                style = MaterialTheme.typography.title2.copy(color = Color.White)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Saat / dakika picker
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Sa ", style = MaterialTheme.typography.body1.copy(color = Color.White))
                Picker(
                    state = hourPickerState,
                    modifier = Modifier.size(width = 60.dp, height = 100.dp)
                ) { optionIndex ->
                    Text(
                        text = hours[optionIndex].toString().padStart(2, '0'),
                        style = MaterialTheme.typography.display1.copy(color = Color.White)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("dk ", style = MaterialTheme.typography.body1.copy(color = Color.White))
                Picker(
                    state = minutePickerState,
                    modifier = Modifier.size(width = 60.dp, height = 100.dp)
                ) { optionIndex ->
                    Text(
                        text = minutes[optionIndex].toString().padStart(2, '0'),
                        style = MaterialTheme.typography.display1.copy(color = Color.White)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            CompositionLocalProvider(LocalContentColor provides Color.White) {
                OutlinedTextField(
                    value = alarmLabel,
                    onValueChange = { alarmLabel = it },
                    label = { Text("Alarm Etiketi") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.body1
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Alarm planlaması
            Button(
                onClick = {
                    // Önce, exact alarm izninin verilip verilmediğini kontrol et
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    if (!alarmManager.canScheduleExactAlarms()) {
                        // Eğer izin yoksa kullanıcıyı ayar ekranına yönlendir
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                            .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                        context.startActivity(intent)
                        return@Button
                    }

                    val chosenHour = hours[selectedHourIndex]
                    val chosenMinute = minutes[selectedMinuteIndex]

                    // Alarm zamanını (bugün için) hesapla
                    val cal = java.util.Calendar.getInstance().apply {
                        timeInMillis = System.currentTimeMillis()
                        set(java.util.Calendar.HOUR_OF_DAY, chosenHour)
                        set(java.util.Calendar.MINUTE, chosenMinute)
                        set(java.util.Calendar.SECOND, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                    }

                    // Geçmiş saate ayarlandıysa alarmı bir sonraki güne taşı
                    if (cal.timeInMillis < System.currentTimeMillis()) {
                        cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
                    }
                    val newAlarmTime = System.currentTimeMillis() + 15 * 1000  // 1 minute from now

                    val alarmId = chosenHour * 100 + chosenMinute
                    com.turkerberktopcu.customalarmapp.presentation.alarm.AlarmScheduler(context)
                        .scheduleAlarm(
                            alarmId = 999,
                            timeInMillis = newAlarmTime,
                            label = "Test Alarm"
                        )

                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Sonraki", color = Color.White)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Preview(device = "id:wearos_small_round", showSystemUi = true)
@Composable
fun AlarmEditScreenPreview() {
    AlarmEditScreen(navController = rememberNavController())
}
