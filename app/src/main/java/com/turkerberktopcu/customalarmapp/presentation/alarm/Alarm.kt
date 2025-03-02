package com.turkerberktopcu.customalarmapp.presentation.alarm

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
// Alarm.kt
@Parcelize
data class Alarm(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val label: String,
    var isEnabled: Boolean,
    var timeInMillis: Long,
    var isDailyReset: Boolean = false,
    var maxSnoozeCount: Int = 0,       // Max allowed snoozes
    var currentSnoozeCount: Int = 0    // Current snooze count
) : Parcelable {

    // Member function to check if alarm should be disabled after snooze
    fun shouldDisableAfterSnooze(): Boolean {
        return maxSnoozeCount > 0 && currentSnoozeCount >= maxSnoozeCount
    }
}