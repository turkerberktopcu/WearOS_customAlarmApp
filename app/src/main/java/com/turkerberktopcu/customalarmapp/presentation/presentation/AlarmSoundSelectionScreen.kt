package com.turkerberktopcu.customalarmapp.presentation.presentation

import android.media.RingtoneManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmSoundSelectionScreen(
    navController: NavController,
    onSoundSelected: (String) -> Unit
) {
    val context = LocalContext.current
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
        topBar = {
            TopAppBar(
                title = { Text("Select Alarm Sound") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(alarmSounds) { sound ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable {
                            // Use the callback to pass back the selected sound
                            onSoundSelected(sound.second)
                            // Then pop back to the previous screen
                            navController.popBackStack()
                        }
                ) {
                    Text(
                        text = sound.first,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
