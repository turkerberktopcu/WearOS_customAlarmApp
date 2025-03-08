package com.turkerberktopcu.customalarmapp.presentation.presentation

import android.media.RingtoneManager
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.items
import androidx.wear.compose.material.rememberScalingLazyListState

@Composable
fun AlarmSoundSelectionScreen(
    navController: NavController,
    onSoundSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScalingLazyListState()

    val ringtoneManager = RingtoneManager(context).apply {
        setType(RingtoneManager.TYPE_ALARM)
    }
    val cursor = ringtoneManager.cursor
    val alarmSounds = remember {
        mutableStateListOf<Pair<String, String>>().apply {
            add("No Sound" to "NO_SOUND")
            while (cursor.moveToNext()) {
                val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                val uri = ringtoneManager.getRingtoneUri(cursor.position).toString()
                add(title to uri)
            }
        }
    }

    Scaffold(
        timeText = { TimeText() },
        positionIndicator = {
            PositionIndicator(
                scalingLazyListState = scrollState
            )
        }
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = scrollState
        ) {
            item {
                Text(
                    "Select Sound",
                    style = MaterialTheme.typography.title2,
                    modifier = Modifier.padding(8.dp)
                )
            }

            items(alarmSounds) { sound ->
                AlarmSoundItem(
                    title = sound.first,
                    onClick = {
                        onSoundSelected(sound.second)
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun AlarmSoundItem(title: String, onClick: () -> Unit) {
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
                text = title,
                style = MaterialTheme.typography.body1,
                color = Color.White
            )
        }
    }
}