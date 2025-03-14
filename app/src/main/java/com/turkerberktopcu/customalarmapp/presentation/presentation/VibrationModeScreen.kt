package com.turkerberktopcu.customalarmapp.presentation.presentation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.material.*
import com.turkerberktopcu.customalarmapp.presentation.alarm.VibrationPattern

// Local extension function for VibrationPattern display name
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
fun VibrationSelectionScreen(navController: NavController, onVibrationSelected: (VibrationPattern) -> Unit) {
    val vibrationModes = listOf(
        VibrationPattern.Default,
        VibrationPattern.Short,
        VibrationPattern.Long,
        VibrationPattern.None
    )
    // Create scroll state for the list
    val scrollState = rememberScalingLazyListState()

    Scaffold(
        timeText = { TimeText() },
        positionIndicator = {
            PositionIndicator(
                scalingLazyListState = scrollState  // Pass the state here
            )
        }
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = scrollState  // Connect the scroll state to the column
        ) {
            item {
                Text(
                    "Select Vibration",
                    style = MaterialTheme.typography.title2,
                    modifier = Modifier.padding(8.dp)
                )
            }
            items(vibrationModes) { pattern ->
                VibrationModeItem(
                    pattern = pattern,
                    onClick = {
                        onVibrationSelected(pattern)
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun VibrationModeItem(pattern: VibrationPattern, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = pattern.getDisplayName(),
                style = MaterialTheme.typography.body1,
                color = Color.White
            )
        }
    }
}
