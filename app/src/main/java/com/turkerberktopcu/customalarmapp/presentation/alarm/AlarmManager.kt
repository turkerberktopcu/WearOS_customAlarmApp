package com.turkerberktopcu.customalarmapp.presentation.alarm

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.turkerberktopcu.customalarmapp.presentation.utils.Constants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import com.turkerberktopcu.customalarmapp.presentation.utils.Constants.DAILY_RESET_ALARM_ID
import com.turkerberktopcu.customalarmapp.presentation.utils.Constants.INVALID_ALARM_ID

class AlarmManager(private val context: Context) {
    private val sharedPrefs = context.getSharedPreferences("alarms", Context.MODE_PRIVATE)
    private val gson = GsonBuilder()
        .registerTypeAdapter(VibrationPattern::class.java, VibrationPatternAdapter())
        .create()
    var alarms = mutableListOf<Alarm>()

    init {
        loadAlarms()
    }

    fun getAllAlarms(): List<Alarm> = alarms.toList()

    fun addAlarm(
        hour: Int,
        minute: Int,
        label: String,
        isDailyReset: Boolean,
        maxSnooze: Int,
        vibrationPattern: VibrationPattern?,
        snoozeInterval: Long  // New parameter
    ): Alarm {
        var newId = if (alarms.isEmpty()) 1 else alarms.maxOf { it.id } + 1

        // Prevent collision with special system alarm IDs
        while (newId == DAILY_RESET_ALARM_ID || newId == INVALID_ALARM_ID) {
            newId++
            Log.w("ID Generation", "Skipped reserved ID, new ID: $newId")
        }

        val newAlarm = Alarm(
            id = newId,
            hour = hour,
            minute = minute,
            label = label,
            isEnabled = true,
            timeInMillis = calculateTriggerTime(hour, minute),
            isDailyReset = isDailyReset,
            maxSnoozeCount = maxSnooze,
            currentSnoozeCount = 0,
            vibrationPattern = vibrationPattern ?: VibrationPattern.None,
            snoozeIntervalMillis = snoozeInterval
        )
        alarms.add(newAlarm)
        saveAlarms()
        return newAlarm
    }


    // In AlarmManager.kt
    fun toggleAlarm(alarmId: Int) {
        alarms.find { it.id == alarmId }?.let { alarm ->
            alarm.isEnabled = !alarm.isEnabled
            if (alarm.isEnabled) {
                alarm.currentSnoozeCount = 0  // Reset snooze count when re-enabled
                alarm.timeInMillis = calculateTriggerTime(alarm.hour, alarm.minute)
            }
            saveAlarms()
        }
    }

    fun handleSnooze(alarmId: Int): Boolean {
        return alarms.find { it.id == alarmId }?.let { alarm ->
            if (alarm.maxSnoozeCount == 0) {
                alarm.currentSnoozeCount++
                saveAlarms()
                true
            } else {
                if (alarm.currentSnoozeCount >= alarm.maxSnoozeCount) {
                    // Disable the alarm when max snoozes reached
                    alarm.isEnabled = false
                    saveAlarms()
                    false
                } else {
                    alarm.currentSnoozeCount++
                    saveAlarms()
                    true
                }
            }
        } ?: false
    }
    fun incrementSnoozeCount(alarmId: Int) {
        alarms.find { it.id == alarmId }?.let {
            it.currentSnoozeCount++
            saveAlarms()
        }
    }

    fun resetSnoozeCount(alarmId: Int) {
        alarms.find { it.id == alarmId }?.let {
            it.currentSnoozeCount = 0
            saveAlarms()
        }
    }
    fun isDailyResetEnabled(): Boolean {
        return sharedPrefs.getBoolean(Constants.DAILY_RESET_ENABLED_PREF, false)
    }

    fun setDailyResetEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(Constants.DAILY_RESET_ENABLED_PREF, enabled).apply()
    }
    fun updateDailyReset(alarmId: Int, isDailyReset: Boolean) {
        alarms.find { it.id == alarmId }?.isDailyReset = isDailyReset
        saveAlarms()
    }
    fun updateAlarmTime(alarmId: Int, newTime: Long) {
        alarms.find { it.id == alarmId }?.let {
            it.timeInMillis = newTime
            saveAlarms()
        }
    }
    fun deleteAlarm(alarmId: Int) {
        alarms.removeAll { it.id == alarmId }
        saveAlarms()
    }

    fun saveAlarms() {
        val json = gson.toJson(alarms)
        sharedPrefs.edit().putString("alarms", json).apply()
    }

    private fun loadAlarms() {
        val json = sharedPrefs.getString("alarms", null)
        alarms = if (json != null) {
            val type = object : TypeToken<List<Alarm>>() {}.type
            gson.fromJson(json, type)
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

    // In AlarmManager.kt
    fun updateAlarm(updatedAlarm: Alarm) {
        val index = alarms.indexOfFirst { it.id == updatedAlarm.id }
        if (index != -1) {
            alarms[index] = updatedAlarm
            saveAlarms()
        }
    }
}