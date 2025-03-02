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
    var maxSnoozeCount: Int = 0,       // Add these
    var currentSnoozeCount: Int = 0    // two new properties
) : Parcelable