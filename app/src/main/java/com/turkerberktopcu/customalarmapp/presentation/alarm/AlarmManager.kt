package com.turkerberktopcu.customalarmapp.presentation.alarm

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class AlarmManager(private val context: Context) {
    private val sharedPrefs = context.getSharedPreferences("alarms", Context.MODE_PRIVATE)
    private val gson = Gson()
    private var alarms = mutableListOf<Alarm>()

    init {
        loadAlarms()
    }

    fun getAllAlarms(): List<Alarm> = alarms.toList()

    fun addAlarm(hour: Int, minute: Int, label: String): Alarm {
        val newId = if (alarms.isEmpty()) 1 else alarms.maxOf { it.id } + 1
        val newAlarm = Alarm(
            id = newId,
            hour = hour,
            minute = minute,
            label = label,
            isEnabled = true,
            timeInMillis = calculateTriggerTime(hour, minute)
        )
        alarms.add(newAlarm)
        saveAlarms()
        return newAlarm
    }

    // In AlarmManager.kt
    fun toggleAlarm(alarmId: Int) {
        alarms.find { it.id == alarmId }?.let { alarm ->
            alarm.isEnabled = !alarm.isEnabled
            // Refresh trigger time when re-enabling
            if (alarm.isEnabled) {
                alarm.timeInMillis = calculateTriggerTime(alarm.hour, alarm.minute)
            }
            saveAlarms()
        }
    }

    fun deleteAlarm(alarmId: Int) {
        alarms.removeAll { it.id == alarmId }
        saveAlarms()
    }

    private fun saveAlarms() {
        val json = gson.toJson(alarms)
        sharedPrefs.edit().putString("alarms", json).apply()
    }

    private fun loadAlarms() {
        val json = sharedPrefs.getString("alarms", null)
        alarms = if (json != null) {
            gson.fromJson(json, object : TypeToken<List<Alarm>>() {}.type)
        } else {
            mutableListOf()
        }
    }

    fun calculateTriggerTime(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // If the time is in the past, add 1 day
            if (timeInMillis < System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        // In calculateTriggerTime()
        Log.d("AlarmTime", "Calculated time: ${calendar.timeInMillis}")
        Log.d("AlarmTime", "Current:    ${SimpleDateFormat("dd-MM HH:mm:ss.SSS").format(Date(System.currentTimeMillis()))}")

        return calendar.timeInMillis
    }
}