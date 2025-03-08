package com.turkerberktopcu.customalarmapp.presentation.alarm

import android.content.Context
import android.media.RingtoneManager
import android.os.Parcelable
import android.os.VibrationEffect
import kotlinx.parcelize.Parcelize
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
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
    var maxSnoozeCount: Int = 0,
    var currentSnoozeCount: Int = 0,
    var vibrationPattern: VibrationPattern = VibrationPattern.None,
    var snoozeIntervalMillis: Long = 180_000L,  // 3-minute snooze interval
    var workingDurationMillis: Long = 300_000L,   // 5-minute working duration
    var breakDurationMillis: Long = 120_000L,       // 2-minute break duration
    var alarmSoundUri: String? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)?.toString() // Default alarm sound
) : Parcelable


// VibrationPattern.kt
@Parcelize
sealed class VibrationPattern : Parcelable {
    @Parcelize
    object Default : VibrationPattern()
    @Parcelize
    object Short : VibrationPattern()
    @Parcelize
    object Long : VibrationPattern()
    @Parcelize
    object Custom : VibrationPattern()
    @Parcelize
    object None : VibrationPattern()

    val name: String get() = when (this) {
        Default -> "DEFAULT"
        Short -> "SHORT"
        Long -> "LONG"
        Custom -> "CUSTOM"
        None -> "NONE"
    }

    fun getEffect(context: Context): VibrationEffect? {
        return when (this) {
            Default -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
            Short -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
            Long -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
            None -> null
            else -> VibrationEffect.createOneShot(1000, 255)
        }
    }
}
class VibrationPatternAdapter : TypeAdapter<VibrationPattern?>() {
    override fun write(out: JsonWriter, value: VibrationPattern?) {
        out.value(value?.name ?: "NONE") // Handle null by writing "NONE"
    }

    override fun read(reader: JsonReader): VibrationPattern? {
        return when (reader.peek()) {
            JsonToken.NULL -> {
                reader.nextNull()
                VibrationPattern.None // Treat null in JSON as VibrationPattern.None
            }
            else -> {
                when (reader.nextString()) {
                    "DEFAULT" -> VibrationPattern.Default
                    "SHORT" -> VibrationPattern.Short
                    "LONG" -> VibrationPattern.Long
                    "CUSTOM" -> VibrationPattern.Custom
                    "NONE" -> VibrationPattern.None
                    else -> VibrationPattern.None
                }
            }
        }
    }
}