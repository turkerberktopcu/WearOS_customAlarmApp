package com.turkerberktopcu.customalarmapp.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.wear.compose.material.MaterialTheme

@Composable
fun AlarmEditScreen(navController: NavController) {
    val hours = (0..23).toList()
    val minutes = (0..59).toList()

    // Picker states
    val hourPickerState = rememberPickerState(hours.size, initiallySelectedOption = 6)
    val minutePickerState = rememberPickerState(minutes.size, initiallySelectedOption = 0)

    // Keep track of selected hour/minute
    var selectedHourIndex by remember { mutableStateOf(hourPickerState.selectedOption) }
    var selectedMinuteIndex by remember { mutableStateOf(minutePickerState.selectedOption) }

    // Update when picker changes
    LaunchedEffect(hourPickerState.selectedOption) {
        selectedHourIndex = hourPickerState.selectedOption
    }
    LaunchedEffect(minutePickerState.selectedOption) {
        selectedMinuteIndex = minutePickerState.selectedOption
    }

    // Alarm label
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
            // Back arrow icon in white
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Geri",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title in white
            Text(
                text = "Saat ayarla",
                style = MaterialTheme.typography.title2.copy(color = Color.White)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Row with hour/minute pickers
            Row(verticalAlignment = Alignment.CenterVertically) {
                // "Sa" label in white
                Text(
                    text = "Sa ",
                    style = MaterialTheme.typography.body1.copy(color = Color.White)
                )

                Picker(
                    state = hourPickerState,
                    modifier = Modifier.size(width = 60.dp, height = 100.dp)
                ) { optionIndex ->
                    // Display hour in white
                    Text(
                        text = hours[optionIndex].toString().padStart(2, '0'),
                        style = MaterialTheme.typography.display1.copy(color = Color.White)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // "dk" label in white
                Text(
                    text = "dk ",
                    style = MaterialTheme.typography.body1.copy(color = Color.White)
                )

                Picker(
                    state = minutePickerState,
                    modifier = Modifier.size(width = 60.dp, height = 100.dp)
                ) { optionIndex ->
                    // Display minute in white
                    Text(
                        text = minutes[optionIndex].toString().padStart(2, '0'),
                        style = MaterialTheme.typography.display1.copy(color = Color.White)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Wrap the OutlinedTextField in a CompositionLocalProvider so that its default text, label, and cursor use white.
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

            // "Sonraki" button with white text
            Button(
                onClick = {
                    val chosenHour = hours[selectedHourIndex]
                    val chosenMinute = minutes[selectedMinuteIndex]
                    // TODO: Save or schedule the alarm using chosenHour, chosenMinute, and alarmLabel.
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Sonraki", color = Color.White)
            }
        }
    }
}

@Preview(device = "id:wearos_small_round", showSystemUi = true)
@Composable
fun AlarmEditScreenPreview() {
    AlarmEditScreen(navController = rememberNavController())
}
