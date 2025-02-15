package com.turkerberktopcu.customalarmapp.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController

@Composable
fun AlarmEditScreen(navController: NavController) {
    var alarmTime by remember { mutableStateOf("") }
    var alarmLabel by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Scaffold(
        timeText = { TimeText() }
    ) {
        // Wear Scaffold içeriği, BoxScope.() -> Unit şeklinde çalışır.
        // Dolayısıyla paddingValues yerine kendimiz .padding(...) ekleyerek düzen yapıyoruz.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // Geri butonu
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = alarmTime,
                onValueChange = { alarmTime = it },
                label = { Text("Alarm Saati (ör: 07:00 AM)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = alarmLabel,
                onValueChange = { alarmLabel = it },
                label = { Text("Alarm Etiketi") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    // Alarmı kaydetme/schedule işlemi burada yapılabilir
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Alarmı Kaydet")
            }
        }
    }
}

@Preview(device = "id:wearos_small_round", showSystemUi = true)
@Composable
fun AlarmEditScreenPreview() {
    AlarmEditScreen(navController = rememberNavController())
}
