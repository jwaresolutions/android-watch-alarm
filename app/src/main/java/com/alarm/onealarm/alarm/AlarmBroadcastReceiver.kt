package com.alarm.onealarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

class AlarmBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("alarm_id", -1)
        val isSnooze = intent.getBooleanExtra("is_snooze", false)
        Log.w("1Alarm", "BroadcastReceiver.onReceive alarmId=$alarmId isSnooze=$isSnooze")

        // Only start foreground service — it will post the FSI notification
        // which is the only reliable way to show UI on Samsung Wear OS
        val serviceIntent = Intent(context, AlarmForegroundService::class.java).apply {
            putExtra("alarm_id", alarmId)
            putExtra("is_snooze", isSnooze)
        }
        ContextCompat.startForegroundService(context, serviceIntent)
        Log.w("1Alarm", "startForegroundService called")
    }
}
